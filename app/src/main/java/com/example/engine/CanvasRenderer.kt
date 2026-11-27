package com.example.engine

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.LruCache
import com.example.model.*
import com.example.ui.theme.FontCatalog
import java.io.File
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object CanvasRenderer {

    private val imageCacheKb = ((Runtime.getRuntime().maxMemory() / 1024L) / 8L).toInt().coerceIn(8192, 49152)

    private val bitmapCache = object : LruCache<String, Bitmap>(imageCacheKb) {
        override fun sizeOf(key: String, value: Bitmap): Int = max(1, value.byteCount / 1024)
    }

    private val previewThumbCache = object : LruCache<String, Bitmap>(16384) {
        override fun sizeOf(key: String, value: Bitmap): Int = max(1, value.byteCount / 1024)
    }

    private val textLayoutCache = LruCache<String, StaticLayout>(64)

    fun invalidateBitmapCache(path: String? = null) {
        if (path == null) {
            bitmapCache.evictAll()
            previewThumbCache.evictAll()
            textLayoutCache.evictAll()
        } else {
            bitmapCache.remove(path)
            previewThumbCache.remove(path)
        }
    }

    fun clearTextCache() {
        textLayoutCache.evictAll()
    }

    fun getCachedPreviewThumbnail(key: String): Bitmap? {
        val bmp = previewThumbCache.get(key)
        return if (bmp != null && !bmp.isRecycled) bmp else null
    }

    fun cachePreviewThumbnail(key: String, bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            previewThumbCache.put(key, bitmap)
        }
    }

    private fun loadCachedBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        bitmapCache.get(path)?.let { if (!it.isRecycled) return it }
        val file = File(path)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(path)?.also {
            bitmapCache.put(path, it)
        }
    }

    private fun buildShader(
        startColor: Int,
        midColor: Int?,
        endColor: Int,
        width: Float,
        height: Float,
        angle: Float,
        type: GradientType
    ): Shader {
        val colors = if (midColor != null) intArrayOf(startColor, midColor, endColor) else intArrayOf(startColor, endColor)
        return if (type == GradientType.RADIAL) {
            RadialGradient(
                width / 2f,
                height / 2f,
                max(10f, max(width, height) * 0.65f),
                colors,
                null,
                Shader.TileMode.CLAMP
            )
        } else {
            val rad = Math.toRadians(angle.toDouble())
            val cx = width / 2f
            val cy = height / 2f
            val halfSpan = max(width, height) / 2f
            val dx = cos(rad).toFloat() * halfSpan
            val dy = sin(rad).toFloat() * halfSpan
            LinearGradient(
                cx - dx, cy - dy,
                cx + dx, cy + dy,
                colors,
                null,
                Shader.TileMode.CLAMP
            )
        }
    }

    private fun applyStrokeEffect(paint: Paint, strokeWidth: Float, style: StrokeStyle) {
        val base = max(4f, strokeWidth)
        paint.pathEffect = when (style) {
            StrokeStyle.SOLID -> null
            StrokeStyle.DASHED -> DashPathEffect(floatArrayOf(base * 2.4f, base * 1.4f), 0f)
            StrokeStyle.DOTTED -> DashPathEffect(floatArrayOf(base * 0.3f, base * 1.6f), 0f)
        }
        if (style == StrokeStyle.DOTTED) {
            paint.strokeCap = Paint.Cap.ROUND
        }
    }

    fun renderDocument(
        context: Context,
        canvas: Canvas,
        document: CanvasDocument,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        transparentBackground: Boolean = false
    ) {
        canvas.save()
        canvas.scale(scaleX, scaleY)

        val w = document.artboardWidth.toFloat()
        val h = document.artboardHeight.toFloat()

        canvas.clipRect(0f, 0f, w, h)

        if (!transparentBackground) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            if (document.backgroundGradientEnd != null) {
                bgPaint.shader = buildShader(
                    startColor = document.backgroundColor.toInt(),
                    midColor = document.backgroundGradientMid?.toInt(),
                    endColor = document.backgroundGradientEnd.toInt(),
                    width = w,
                    height = h,
                    angle = 45f,
                    type = document.backgroundGradientType
                )
            } else {
                bgPaint.color = document.backgroundColor.toInt()
            }
            canvas.drawRect(0f, 0f, w, h, bgPaint)

            val bgImgPath = document.backgroundImagePath
            if (!bgImgPath.isNullOrBlank()) {
                val bgBmp = loadCachedBitmap(bgImgPath)
                if (bgBmp != null) {
                    val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(bgBmp, null, RectF(0f, 0f, w, h), bmpPaint)
                }
            }
        }

        document.layers.forEach { layer ->
            if (!layer.visible) return@forEach
            drawLayer(context, canvas, layer)
        }

        canvas.restore()
    }

    fun drawLayer(context: Context, canvas: Canvas, layer: LayerElement) {
        val alphaInt = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
        val cx = layer.x + layer.width / 2f
        val cy = layer.y + layer.height / 2f

        canvas.save()
        if (layer.rotation != 0f) {
            canvas.rotate(layer.rotation, cx, cy)
        }
        canvas.translate(layer.x, layer.y)
        if (layer.flipH || layer.flipV) {
            canvas.scale(
                if (layer.flipH) -1f else 1f,
                if (layer.flipV) -1f else 1f,
                layer.width / 2f,
                layer.height / 2f
            )
        }

        // Optional 3D extrusion pass for depth effect
        if (layer.effects.is3dEffect) {
            val steps = 5
            for (step in steps downTo 1) {
                canvas.save()
                canvas.translate(step * 2.5f, step * 2.5f)
                drawLayerContent(context, canvas, layer, (alphaInt * 0.28f).toInt(), shadowOverrideColor = 0xFF2B2B2EL.toInt())
                canvas.restore()
            }
        }

        drawLayerContent(context, canvas, layer, alphaInt, shadowOverrideColor = null)
        canvas.restore()
    }

    private fun drawLayerContent(
        context: Context,
        canvas: Canvas,
        layer: LayerElement,
        alphaInt: Int,
        shadowOverrideColor: Int?
    ) {
        when (layer) {
            is ShapeLayer -> drawShapeLayer(canvas, layer, alphaInt, shadowOverrideColor)
            is PathLayer -> drawPathLayer(canvas, layer, alphaInt, shadowOverrideColor)
            is TextLayer -> drawTextLayer(context, canvas, layer, alphaInt, shadowOverrideColor)
            is ImageLayer -> drawImageLayer(canvas, layer, alphaInt)
            is StickerLayer -> {
                if (layer.effects.glowEnabled) {
                    canvas.save()
                    canvas.scale(1.03f, 1.03f, layer.width / 2f, layer.height / 2f)
                    StickerRenderer.drawSticker(
                        context = context,
                        canvas = canvas,
                        stickerId = layer.stickerId,
                        width = layer.width,
                        height = layer.height,
                        primaryColor = layer.effects.glowColor.toInt(),
                        secondaryColor = layer.effects.glowColor.toInt(),
                        alpha = (alphaInt * 0.45f).toInt()
                    )
                    canvas.restore()
                }
                if (layer.effects.shadowEnabled && shadowOverrideColor == null) {
                    canvas.save()
                    canvas.translate(layer.effects.shadowOffsetX, layer.effects.shadowOffsetY)
                    StickerRenderer.drawSticker(
                        context = context,
                        canvas = canvas,
                        stickerId = layer.stickerId,
                        width = layer.width,
                        height = layer.height,
                        primaryColor = layer.effects.shadowColor.toInt(),
                        secondaryColor = layer.effects.shadowColor.toInt(),
                        alpha = (alphaInt * 0.42f).toInt()
                    )
                    canvas.restore()
                }
                StickerRenderer.drawSticker(
                    context = context,
                    canvas = canvas,
                    stickerId = layer.stickerId,
                    width = layer.width,
                    height = layer.height,
                    primaryColor = shadowOverrideColor ?: layer.primaryColor.toInt(),
                    secondaryColor = shadowOverrideColor ?: layer.secondaryColor.toInt(),
                    alpha = alphaInt
                )
            }
        }
    }

    private fun configureEffectsPaint(paint: Paint, effects: LayerEffects) {
        if (effects.blurRadius > 0.5f) {
            paint.maskFilter = BlurMaskFilter(effects.blurRadius.coerceAtMost(40f), BlurMaskFilter.Blur.NORMAL)
        } else if (effects.glowEnabled) {
            paint.maskFilter = BlurMaskFilter(effects.glowRadius.coerceIn(2f, 40f), BlurMaskFilter.Blur.SOLID)
            paint.setShadowLayer(effects.glowRadius, 0f, 0f, effects.glowColor.toInt())
        } else if (effects.shadowEnabled) {
            paint.setShadowLayer(
                effects.shadowBlur.coerceAtLeast(1f),
                effects.shadowOffsetX,
                effects.shadowOffsetY,
                effects.shadowColor.toInt()
            )
        }
    }

    private fun drawShapeLayer(
        canvas: Canvas,
        layer: ShapeLayer,
        alphaInt: Int,
        shadowOverrideColor: Int?
    ) {
        val w = layer.width
        val h = layer.height
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            if (shadowOverrideColor != null) {
                color = shadowOverrideColor
                alpha = alphaInt
            } else if (layer.gradientEndColor != null) {
                shader = buildShader(
                    startColor = layer.fillColor.toInt(),
                    midColor = layer.gradientMidColor?.toInt(),
                    endColor = layer.gradientEndColor.toInt(),
                    width = w,
                    height = h,
                    angle = layer.gradientAngle,
                    type = layer.gradientType
                )
                alpha = alphaInt
            } else {
                val baseColor = layer.fillColor.toInt()
                color = baseColor
                val origA = android.graphics.Color.alpha(baseColor)
                alpha = (origA * (alphaInt / 255f)).toInt()
            }
            if (shadowOverrideColor == null) configureEffectsPaint(this, layer.effects)
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = shadowOverrideColor ?: layer.strokeColor.toInt()
            strokeWidth = layer.strokeWidth
            strokeJoin = Paint.Join.ROUND
            val origA = android.graphics.Color.alpha(layer.strokeColor.toInt())
            alpha = (origA * (alphaInt / 255f)).toInt()
            applyStrokeEffect(this, layer.strokeWidth, layer.strokeStyle)
        }

        val outlineEffectPaint = if (layer.effects.outlineWidth > 0f && shadowOverrideColor == null) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = layer.effects.outlineColor.toInt()
                strokeWidth = layer.effects.outlineWidth
                strokeJoin = Paint.Join.ROUND
                alpha = alphaInt
            }
        } else null

        val hasFill = android.graphics.Color.alpha(layer.fillColor.toInt()) > 0 || layer.gradientEndColor != null
        val hasStroke = layer.strokeWidth > 0f && android.graphics.Color.alpha(layer.strokeColor.toInt()) > 0

        when (layer.shapeType) {
            ShapeType.RECTANGLE, ShapeType.ROUNDED_RECT -> {
                val rect = RectF(0f, 0f, w, h)
                val rad = if (layer.shapeType == ShapeType.ROUNDED_RECT && layer.cornerRadius <= 0f) {
                    min(w, h) * 0.16f
                } else layer.cornerRadius
                if (hasFill) canvas.drawRoundRect(rect, rad, rad, fillPaint)
                if (hasStroke) canvas.drawRoundRect(rect, rad, rad, strokePaint)
                if (outlineEffectPaint != null) canvas.drawRoundRect(rect, rad, rad, outlineEffectPaint)
            }
            ShapeType.CIRCLE -> {
                val rect = RectF(0f, 0f, w, h)
                if (hasFill) canvas.drawOval(rect, fillPaint)
                if (hasStroke) canvas.drawOval(rect, strokePaint)
                if (outlineEffectPaint != null) canvas.drawOval(rect, outlineEffectPaint)
            }
            ShapeType.ARC -> {
                val rect = RectF(4f, 4f, max(8f, w - 4f), max(8f, h - 4f))
                val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = shadowOverrideColor ?: if (hasStroke) layer.strokeColor.toInt() else layer.fillColor.toInt()
                    strokeWidth = max(10f, layer.strokeWidth)
                    strokeCap = Paint.Cap.ROUND
                    alpha = alphaInt
                    applyStrokeEffect(this, strokeWidth, layer.strokeStyle)
                    if (shadowOverrideColor == null) configureEffectsPaint(this, layer.effects)
                }
                canvas.drawArc(rect, -135f, layer.arcSweepAngle, false, arcPaint)
            }
            ShapeType.TRIANGLE -> {
                val p = Path().apply {
                    moveTo(w / 2f, 0f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                if (hasFill) canvas.drawPath(p, fillPaint)
                if (hasStroke) canvas.drawPath(p, strokePaint)
                if (outlineEffectPaint != null) canvas.drawPath(p, outlineEffectPaint)
            }
            ShapeType.LINE -> {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = shadowOverrideColor ?: if (hasFill) layer.fillColor.toInt() else layer.strokeColor.toInt()
                    strokeWidth = max(6f, layer.strokeWidth)
                    strokeCap = Paint.Cap.ROUND
                    alpha = alphaInt
                    applyStrokeEffect(this, strokeWidth, layer.strokeStyle)
                    if (shadowOverrideColor == null) configureEffectsPaint(this, layer.effects)
                }
                canvas.drawLine(0f, h / 2f, w, h / 2f, linePaint)
            }
            ShapeType.STAR -> {
                val r = min(w, h) / 2f
                if (hasFill) StickerRenderer.drawStarPolygon(canvas, w / 2f, h / 2f, r, r * 0.45f, 5, fillPaint)
                if (hasStroke) StickerRenderer.drawStarPolygon(canvas, w / 2f, h / 2f, r, r * 0.45f, 5, strokePaint)
                if (outlineEffectPaint != null) StickerRenderer.drawStarPolygon(canvas, w / 2f, h / 2f, r, r * 0.45f, 5, outlineEffectPaint)
            }
            ShapeType.POLYGON -> {
                val r = min(w, h) / 2f
                if (hasFill) StickerRenderer.drawRegularPolygon(canvas, w / 2f, h / 2f, r, layer.polygonSides, fillPaint)
                if (hasStroke) StickerRenderer.drawRegularPolygon(canvas, w / 2f, h / 2f, r, layer.polygonSides, strokePaint)
                if (outlineEffectPaint != null) StickerRenderer.drawRegularPolygon(canvas, w / 2f, h / 2f, r, layer.polygonSides, outlineEffectPaint)
            }
        }
    }

    private fun drawPathLayer(
        canvas: Canvas,
        layer: PathLayer,
        alphaInt: Int,
        shadowOverrideColor: Int?
    ) {
        if (layer.points.size < 2) return
        val path = buildSmoothPath(layer.points, layer.isPenSmooth, layer.isClosed)

        if (layer.fillColor != null) {
            val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = shadowOverrideColor ?: layer.fillColor.toInt()
                alpha = alphaInt
                if (shadowOverrideColor == null) configureEffectsPaint(this, layer.effects)
            }
            canvas.drawPath(path, fp)
        }

        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = shadowOverrideColor ?: layer.strokeColor.toInt()
            strokeWidth = layer.strokeWidth
            strokeCap = if (layer.brushStyle == BrushStyle.MARKER) Paint.Cap.SQUARE else Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            alpha = alphaInt
            applyStrokeEffect(this, layer.strokeWidth, layer.strokeStyle)
            if (shadowOverrideColor == null) configureEffectsPaint(this, layer.effects)
        }
        canvas.drawPath(path, sp)

        if (layer.effects.outlineWidth > 0f && shadowOverrideColor == null) {
            val op = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = layer.effects.outlineColor.toInt()
                strokeWidth = layer.strokeWidth + layer.effects.outlineWidth
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                alpha = (alphaInt * 0.65f).toInt()
            }
            canvas.drawPath(path, op)
            canvas.drawPath(path, sp)
        }
    }

    fun buildSmoothPath(points: List<PathPoint>, smooth: Boolean, closed: Boolean): Path {
        val path = Path()
        if (points.isEmpty()) return path
        path.moveTo(points[0].x, points[0].y)
        if (!smooth || points.size < 3) {
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
        } else {
            for (i in 1 until points.size - 1) {
                val midX = (points[i].x + points[i + 1].x) / 2f
                val midY = (points[i].y + points[i + 1].y) / 2f
                path.quadTo(points[i].x, points[i].y, midX, midY)
            }
            val last = points.last()
            path.lineTo(last.x, last.y)
        }
        if (closed) path.close()
        return path
    }

    private fun drawTextLayer(
        context: Context,
        canvas: Canvas,
        layer: TextLayer,
        alphaInt: Int,
        shadowOverrideColor: Int?
    ) {
        val fontOpt = FontCatalog.findById(layer.fontId)
        val baseTypeface = FontCatalog.getBaseTypeface(context, fontOpt)
        val effectiveBold = layer.bold || fontOpt.defaultBold
        val effectiveItalic = layer.italic || fontOpt.defaultItalic
        val effectiveLetterSpacing = fontOpt.letterSpacingEm + layer.letterSpacing

        if (layer.textBackgroundColor != null && shadowOverrideColor == null) {
            val bgP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.textBackgroundColor.toInt()
                alpha = alphaInt
            }
            canvas.drawRoundRect(
                RectF(-12f, -8f, layer.width + 12f, layer.height + 8f),
                16f, 16f, bgP
            )
        }

        // Curved Text rendering along an arc path when curveAmount != 0
        if (abs(layer.curveAmount) > 0.02f) {
            val arcPath = Path().apply {
                val bend = layer.curveAmount * layer.height * 0.85f
                val midY = layer.height / 2f
                moveTo(0f, midY + bend * 0.4f)
                quadTo(layer.width / 2f, midY - bend, layer.width, midY + bend * 0.4f)
            }
            if (layer.strokeWidth > 0f && shadowOverrideColor == null) {
                val stp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = baseTypeface
                    isFakeBoldText = effectiveBold
                    textSkewX = if (effectiveItalic) -0.22f else 0f
                    isUnderlineText = layer.underline
                    textSize = layer.fontSize
                    style = Paint.Style.STROKE
                    strokeWidth = layer.strokeWidth
                    strokeJoin = Paint.Join.ROUND
                    color = layer.strokeColor.toInt()
                    letterSpacing = effectiveLetterSpacing
                    textAlign = Paint.Align.CENTER
                    alpha = alphaInt
                }
                canvas.drawTextOnPath(layer.text, arcPath, 0f, layer.fontSize * 0.25f, stp)
            }
            val ftp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = baseTypeface
                isFakeBoldText = effectiveBold
                textSkewX = if (effectiveItalic) -0.22f else 0f
                isUnderlineText = layer.underline
                textSize = layer.fontSize
                letterSpacing = effectiveLetterSpacing
                textAlign = Paint.Align.CENTER
                if (shadowOverrideColor != null) {
                    color = shadowOverrideColor
                } else if (layer.gradientEndColor != null) {
                    shader = buildShader(
                        startColor = layer.color.toInt(),
                        midColor = layer.gradientMidColor?.toInt(),
                        endColor = layer.gradientEndColor.toInt(),
                        width = layer.width,
                        height = layer.height,
                        angle = layer.gradientAngle,
                        type = layer.gradientType
                    )
                } else {
                    color = layer.color.toInt()
                }
                alpha = alphaInt
                if (shadowOverrideColor == null) {
                    if (layer.shadowEnabled || layer.effects.shadowEnabled) {
                        val sRadius = if (layer.effects.shadowEnabled) layer.effects.shadowBlur else layer.shadowRadius
                        val sx = if (layer.effects.shadowEnabled) layer.effects.shadowOffsetX else layer.shadowOffsetX
                        val sy = if (layer.effects.shadowEnabled) layer.effects.shadowOffsetY else layer.shadowOffsetY
                        val sc = if (layer.effects.shadowEnabled) layer.effects.shadowColor else layer.shadowColor
                        setShadowLayer(sRadius, sx, sy, sc.toInt())
                    } else if (layer.effects.glowEnabled) {
                        setShadowLayer(layer.effects.glowRadius, 0f, 0f, layer.effects.glowColor.toInt())
                    }
                }
            }
            canvas.drawTextOnPath(layer.text, arcPath, 0f, layer.fontSize * 0.25f, ftp)
            return
        }

        val alignment = when (layer.align) {
            TextAlignMode.LEFT -> Layout.Alignment.ALIGN_NORMAL
            TextAlignMode.CENTER -> Layout.Alignment.ALIGN_CENTER
            TextAlignMode.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
        }

        val layoutW = max(20, layer.width.toInt())
        val effectiveStroke = if (layer.strokeWidth > 0f) layer.strokeWidth else layer.effects.outlineWidth
        val effectiveStrokeCol = if (layer.strokeWidth > 0f) layer.strokeColor else layer.effects.outlineColor

        if (effectiveStroke > 0f && shadowOverrideColor == null) {
            val strokeKey = "s|${layer.text}|${layer.fontId}|${layer.fontSize}|$layoutW|${layer.align}|$effectiveBold|$effectiveItalic|${layer.underline}|${layer.lineSpacing}|$effectiveLetterSpacing|$effectiveStroke|$effectiveStrokeCol|$alphaInt"
            val strokeLayout = textLayoutCache.get(strokeKey) ?: run {
                val strokeTp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.typeface = baseTypeface
                    isFakeBoldText = effectiveBold
                    textSkewX = if (effectiveItalic) -0.22f else 0f
                    isUnderlineText = layer.underline
                    textSize = layer.fontSize
                    style = Paint.Style.STROKE
                    strokeWidth = effectiveStroke
                    strokeJoin = Paint.Join.ROUND
                    color = effectiveStrokeCol.toInt()
                    letterSpacing = effectiveLetterSpacing
                    alpha = alphaInt
                }
                StaticLayout.Builder.obtain(
                    layer.text, 0, layer.text.length, strokeTp, layoutW
                ).setAlignment(alignment)
                    .setLineSpacing(0f, layer.lineSpacing.coerceIn(0.6f, 2.5f))
                    .setIncludePad(false)
                    .build().also {
                        textLayoutCache.put(strokeKey, it)
                    }
            }
            strokeLayout.draw(canvas)
        }

        val fillKey = "f|${layer.text}|${layer.fontId}|${layer.fontSize}|$layoutW|${layer.height.toInt()}|${layer.align}|$effectiveBold|$effectiveItalic|${layer.underline}|${layer.lineSpacing}|$effectiveLetterSpacing|${layer.color}|${layer.gradientMidColor}|${layer.gradientEndColor}|${layer.gradientType}|${layer.shadowEnabled}|${layer.effects}|$shadowOverrideColor|$alphaInt"
        val layout = textLayoutCache.get(fillKey) ?: run {
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = baseTypeface
                isFakeBoldText = effectiveBold
                textSkewX = if (effectiveItalic) -0.22f else 0f
                isUnderlineText = layer.underline
                textSize = layer.fontSize
                letterSpacing = effectiveLetterSpacing
                if (shadowOverrideColor != null) {
                    color = shadowOverrideColor
                } else if (layer.gradientEndColor != null) {
                    shader = buildShader(
                        startColor = layer.color.toInt(),
                        midColor = layer.gradientMidColor?.toInt(),
                        endColor = layer.gradientEndColor.toInt(),
                        width = layer.width,
                        height = layer.height,
                        angle = layer.gradientAngle,
                        type = layer.gradientType
                    )
                } else {
                    color = layer.color.toInt()
                }
                alpha = alphaInt
                if (shadowOverrideColor == null) {
                    if (layer.shadowEnabled || layer.effects.shadowEnabled) {
                        val sRadius = if (layer.effects.shadowEnabled) layer.effects.shadowBlur else layer.shadowRadius
                        val sx = if (layer.effects.shadowEnabled) layer.effects.shadowOffsetX else layer.shadowOffsetX
                        val sy = if (layer.effects.shadowEnabled) layer.effects.shadowOffsetY else layer.shadowOffsetY
                        val sc = if (layer.effects.shadowEnabled) layer.effects.shadowColor else layer.shadowColor
                        setShadowLayer(max(1f, sRadius), sx, sy, sc.toInt())
                    } else if (layer.effects.glowEnabled) {
                        setShadowLayer(max(2f, layer.effects.glowRadius), 0f, 0f, layer.effects.glowColor.toInt())
                    }
                    if (layer.effects.blurRadius > 0.5f) {
                        maskFilter = BlurMaskFilter(layer.effects.blurRadius.coerceAtMost(32f), BlurMaskFilter.Blur.NORMAL)
                    }
                }
            }
            StaticLayout.Builder.obtain(
                layer.text, 0, layer.text.length, tp, layoutW
            ).setAlignment(alignment)
                .setLineSpacing(0f, layer.lineSpacing.coerceIn(0.6f, 2.5f))
                .setIncludePad(false)
                .build().also {
                    textLayoutCache.put(fillKey, it)
                }
        }
        layout.draw(canvas)
    }

    private fun drawImageLayer(canvas: Canvas, layer: ImageLayer, alphaInt: Int) {
        val bmp = loadCachedBitmap(layer.imagePath) ?: return
        val srcLeft = (bmp.width * layer.cropLeft.coerceIn(0f, 0.9f)).toInt()
        val srcTop = (bmp.height * layer.cropTop.coerceIn(0f, 0.9f)).toInt()
        val srcRight = (bmp.width * layer.cropRight.coerceIn(layer.cropLeft + 0.05f, 1f)).toInt()
        val srcBottom = (bmp.height * layer.cropBottom.coerceIn(layer.cropTop + 0.05f, 1f)).toInt()

        val srcRect = Rect(srcLeft, srcTop, srcRight, srcBottom)
        val dstRect = RectF(0f, 0f, layer.width, layer.height)

        if (layer.effects.shadowEnabled) {
            val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.effects.shadowColor.toInt()
                alpha = (alphaInt * 0.45f).toInt()
                maskFilter = BlurMaskFilter(max(2f, layer.effects.shadowBlur), BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawRect(
                RectF(
                    layer.effects.shadowOffsetX,
                    layer.effects.shadowOffsetY,
                    layer.width + layer.effects.shadowOffsetX,
                    layer.height + layer.effects.shadowOffsetY
                ),
                sp
            )
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
            alpha = alphaInt
            if (layer.effects.blurRadius > 0.5f) {
                maskFilter = BlurMaskFilter(layer.effects.blurRadius.coerceAtMost(24f), BlurMaskFilter.Blur.NORMAL)
            }
        }
        if (layer.cornerRadiusRatio > 0.001f) {
            canvas.save()
            val rx = min(layer.width, layer.height) * layer.cornerRadiusRatio.coerceIn(0f, 0.5f)
            val ry = if (layer.cornerRadiusRatio >= 0.499f) layer.height / 2f else rx
            val finalRx = if (layer.cornerRadiusRatio >= 0.499f) layer.width / 2f else rx
            val clipPath = Path().apply {
                addRoundRect(dstRect, finalRx, ry, Path.Direction.CW)
            }
            canvas.clipPath(clipPath)
            canvas.drawBitmap(bmp, srcRect, dstRect, paint)
            canvas.restore()
        } else {
            canvas.drawBitmap(bmp, srcRect, dstRect, paint)
        }

        if (layer.effects.outlineWidth > 0f) {
            val op = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = layer.effects.outlineColor.toInt()
                strokeWidth = layer.effects.outlineWidth
                alpha = alphaInt
            }
            canvas.drawRect(dstRect, op)
        }
    }

    fun renderThumbnail(context: Context, doc: CanvasDocument, maxDimension: Int = 360): Bitmap {
        val ratio = maxDimension.toFloat() / max(doc.artboardWidth, doc.artboardHeight).toFloat()
        val w = max(1, (doc.artboardWidth * ratio).toInt())
        val h = max(1, (doc.artboardHeight * ratio).toInt())
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        renderDocument(context, canvas, doc, scaleX = ratio, scaleY = ratio, transparentBackground = false)
        return bmp
    }

    fun resolveSafeExportDimensions(targetWidth: Int, targetHeight: Int): Pair<Int, Int> {
        var w = targetWidth.coerceIn(50, 20000)
        var h = targetHeight.coerceIn(50, 20000)
        val maxPixels = 8192L * 4608L
        if (w.toLong() * h.toLong() > maxPixels) {
            val factor = kotlin.math.sqrt(maxPixels.toDouble() / (w.toLong() * h.toLong()).toDouble()).toFloat()
            w = max(1, (w * factor).toInt())
            h = max(1, (h * factor).toInt())
        }
        return w to h
    }

    fun renderExportBitmap(
        context: Context,
        doc: CanvasDocument,
        resolution: ExportResolution,
        transparentBackground: Boolean
    ): Bitmap {
        val baseW = max(doc.artboardWidth, doc.targetExportWidth)
        val baseH = max(doc.artboardHeight, doc.targetExportHeight)
        val targetScale = if (resolution.isFixedMaxEdge) {
            resolution.multiplierOrTargetMax.toFloat() / max(baseW, baseH).toFloat()
        } else {
            resolution.multiplierOrTargetMax.toFloat()
        }

        val (w, h) = resolveSafeExportDimensions(
            targetWidth = max(1, (baseW * targetScale).toInt()),
            targetHeight = max(1, (baseH * targetScale).toInt())
        )

        val bmp = try {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            Bitmap.createBitmap(max(1, w / 2), max(1, h / 2), Bitmap.Config.ARGB_8888)
        }
        val actualScaleX = bmp.width.toFloat() / doc.artboardWidth.toFloat()
        val actualScaleY = bmp.height.toFloat() / doc.artboardHeight.toFloat()

        val canvas = Canvas(bmp)
        renderDocument(context, canvas, doc, actualScaleX, actualScaleY, transparentBackground)
        return bmp
    }

    fun renderCustomResolutionBitmap(
        context: Context,
        doc: CanvasDocument,
        customWidth: Int,
        customHeight: Int,
        transparentBackground: Boolean,
        stretchToFit: Boolean = false
    ): Bitmap {
        val (w, h) = resolveSafeExportDimensions(customWidth, customHeight)
        val bmp = try {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            Bitmap.createBitmap(max(1, w / 2), max(1, h / 2), Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bmp)

        if (!transparentBackground) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            if (doc.backgroundGradientEnd != null) {
                bgPaint.shader = buildShader(
                    startColor = doc.backgroundColor.toInt(),
                    midColor = doc.backgroundGradientMid?.toInt(),
                    endColor = doc.backgroundGradientEnd.toInt(),
                    width = bmp.width.toFloat(),
                    height = bmp.height.toFloat(),
                    angle = 45f,
                    type = doc.backgroundGradientType
                )
            } else {
                bgPaint.color = doc.backgroundColor.toInt()
            }
            canvas.drawRect(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat(), bgPaint)
        }

        if (stretchToFit) {
            val sx = bmp.width.toFloat() / doc.artboardWidth.toFloat()
            val sy = bmp.height.toFloat() / doc.artboardHeight.toFloat()
            renderDocument(context, canvas, doc, sx, sy, transparentBackground)
        } else {
            val fitScale = min(
                bmp.width.toFloat() / doc.artboardWidth.toFloat(),
                bmp.height.toFloat() / doc.artboardHeight.toFloat()
            )
            val offsetX = (bmp.width - doc.artboardWidth * fitScale) / 2f
            val offsetY = (bmp.height - doc.artboardHeight * fitScale) / 2f
            canvas.save()
            canvas.translate(offsetX, offsetY)
            renderDocument(context, canvas, doc, fitScale, fitScale, transparentBackground)
            canvas.restore()
        }
        return bmp
    }
}
