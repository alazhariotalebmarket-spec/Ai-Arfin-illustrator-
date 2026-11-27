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

        // Clip to artboard bounds
        canvas.clipRect(0f, 0f, w, h)

        if (!transparentBackground) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            if (document.backgroundGradientEnd != null) {
                bgPaint.shader = LinearGradient(
                    0f, 0f, w, h,
                    document.backgroundColor.toInt(),
                    document.backgroundGradientEnd.toInt(),
                    Shader.TileMode.CLAMP
                )
            } else {
                bgPaint.color = document.backgroundColor.toInt()
            }
            canvas.drawRect(0f, 0f, w, h, bgPaint)
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

        when (layer) {
            is ShapeLayer -> drawShapeLayer(canvas, layer, alphaInt)
            is PathLayer -> drawPathLayer(canvas, layer, alphaInt)
            is TextLayer -> drawTextLayer(context, canvas, layer, alphaInt)
            is ImageLayer -> drawImageLayer(canvas, layer, alphaInt)
            is StickerLayer -> StickerRenderer.drawSticker(
                context = context,
                canvas = canvas,
                stickerId = layer.stickerId,
                width = layer.width,
                height = layer.height,
                primaryColor = layer.primaryColor.toInt(),
                secondaryColor = layer.secondaryColor.toInt(),
                alpha = alphaInt
            )
        }
        canvas.restore()
    }

    private fun drawShapeLayer(canvas: Canvas, layer: ShapeLayer, alphaInt: Int) {
        val w = layer.width
        val h = layer.height
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            if (layer.gradientEndColor != null) {
                val rad = Math.toRadians(layer.gradientAngle.toDouble())
                val dx = cos(rad).toFloat() * w
                val dy = sin(rad).toFloat() * h
                shader = LinearGradient(
                    0f, 0f, dx, dy,
                    layer.fillColor.toInt(),
                    layer.gradientEndColor.toInt(),
                    Shader.TileMode.CLAMP
                )
                alpha = alphaInt
            } else {
                val baseColor = layer.fillColor.toInt()
                color = baseColor
                val origA = android.graphics.Color.alpha(baseColor)
                alpha = (origA * (alphaInt / 255f)).toInt()
            }
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = layer.strokeColor.toInt()
            strokeWidth = layer.strokeWidth
            val origA = android.graphics.Color.alpha(layer.strokeColor.toInt())
            alpha = (origA * (alphaInt / 255f)).toInt()
        }

        val hasFill = android.graphics.Color.alpha(layer.fillColor.toInt()) > 0 || layer.gradientEndColor != null
        val hasStroke = layer.strokeWidth > 0f && android.graphics.Color.alpha(layer.strokeColor.toInt()) > 0

        when (layer.shapeType) {
            ShapeType.RECTANGLE -> {
                val rect = RectF(0f, 0f, w, h)
                if (hasFill) canvas.drawRoundRect(rect, layer.cornerRadius, layer.cornerRadius, fillPaint)
                if (hasStroke) canvas.drawRoundRect(rect, layer.cornerRadius, layer.cornerRadius, strokePaint)
            }
            ShapeType.CIRCLE -> {
                val rect = RectF(0f, 0f, w, h)
                if (hasFill) canvas.drawOval(rect, fillPaint)
                if (hasStroke) canvas.drawOval(rect, strokePaint)
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
            }
            ShapeType.LINE -> {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = if (hasFill) layer.fillColor.toInt() else layer.strokeColor.toInt()
                    strokeWidth = max(6f, layer.strokeWidth)
                    strokeCap = Paint.Cap.ROUND
                    alpha = alphaInt
                }
                canvas.drawLine(0f, h / 2f, w, h / 2f, linePaint)
            }
            ShapeType.STAR -> {
                val r = min(w, h) / 2f
                if (hasFill) StickerRenderer.drawStarPolygon(canvas, w / 2f, h / 2f, r, r * 0.45f, 5, fillPaint)
                if (hasStroke) StickerRenderer.drawStarPolygon(canvas, w / 2f, h / 2f, r, r * 0.45f, 5, strokePaint)
            }
            ShapeType.POLYGON -> {
                val r = min(w, h) / 2f
                if (hasFill) StickerRenderer.drawRegularPolygon(canvas, w / 2f, h / 2f, r, layer.polygonSides, fillPaint)
                if (hasStroke) StickerRenderer.drawRegularPolygon(canvas, w / 2f, h / 2f, r, layer.polygonSides, strokePaint)
            }
        }
    }

    private fun drawPathLayer(canvas: Canvas, layer: PathLayer, alphaInt: Int) {
        if (layer.points.size < 2) return
        val path = buildSmoothPath(layer.points, layer.isPenSmooth, layer.isClosed)

        if (layer.fillColor != null) {
            val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = layer.fillColor.toInt()
                alpha = alphaInt
            }
            canvas.drawPath(path, fp)
        }

        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = layer.strokeColor.toInt()
            strokeWidth = layer.strokeWidth
            strokeCap = if (layer.brushStyle == BrushStyle.MARKER) Paint.Cap.SQUARE else Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            alpha = alphaInt
        }
        canvas.drawPath(path, sp)
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

    private fun drawTextLayer(context: Context, canvas: Canvas, layer: TextLayer, alphaInt: Int) {
        val fontOpt = FontCatalog.findById(layer.fontId)
        val baseTypeface = FontCatalog.getBaseTypeface(context, fontOpt)
        val effectiveBold = layer.bold || fontOpt.defaultBold
        val effectiveItalic = layer.italic || fontOpt.defaultItalic
        val alignment = when (layer.align) {
            TextAlignMode.LEFT -> Layout.Alignment.ALIGN_NORMAL
            TextAlignMode.CENTER -> Layout.Alignment.ALIGN_CENTER
            TextAlignMode.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
        }

        val layoutW = max(20, layer.width.toInt())

        if (layer.strokeWidth > 0f) {
            val strokeKey = "s|${layer.text}|${layer.fontId}|${layer.fontSize}|$layoutW|${layer.align}|$effectiveBold|$effectiveItalic|${layer.strokeWidth}|${layer.strokeColor}|$alphaInt"
            val strokeLayout = textLayoutCache.get(strokeKey) ?: run {
                val strokeTp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.typeface = baseTypeface
                    isFakeBoldText = effectiveBold
                    textSkewX = if (effectiveItalic) -0.22f else 0f
                    textSize = layer.fontSize
                    style = Paint.Style.STROKE
                    strokeWidth = layer.strokeWidth
                    strokeJoin = Paint.Join.ROUND
                    color = layer.strokeColor.toInt()
                    letterSpacing = fontOpt.letterSpacingEm
                    alpha = alphaInt
                }
                StaticLayout.Builder.obtain(
                    layer.text, 0, layer.text.length, strokeTp, layoutW
                ).setAlignment(alignment).setIncludePad(false).build().also {
                    textLayoutCache.put(strokeKey, it)
                }
            }
            strokeLayout.draw(canvas)
        }

        val fillKey = "f|${layer.text}|${layer.fontId}|${layer.fontSize}|$layoutW|${layer.height.toInt()}|${layer.align}|$effectiveBold|$effectiveItalic|${layer.color}|${layer.gradientEndColor}|${layer.shadowEnabled}|$alphaInt"
        val layout = textLayoutCache.get(fillKey) ?: run {
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = baseTypeface
                isFakeBoldText = effectiveBold
                textSkewX = if (effectiveItalic) -0.22f else 0f
                textSize = layer.fontSize
                letterSpacing = fontOpt.letterSpacingEm
                if (layer.gradientEndColor != null) {
                    shader = LinearGradient(
                        0f, 0f, layer.width, layer.height,
                        layer.color.toInt(), layer.gradientEndColor.toInt(),
                        Shader.TileMode.CLAMP
                    )
                } else {
                    color = layer.color.toInt()
                }
                alpha = alphaInt
                if (layer.shadowEnabled) {
                    setShadowLayer(
                        layer.shadowRadius,
                        layer.shadowOffsetX,
                        layer.shadowOffsetY,
                        layer.shadowColor.toInt()
                    )
                }
            }
            StaticLayout.Builder.obtain(
                layer.text, 0, layer.text.length, tp, layoutW
            ).setAlignment(alignment).setIncludePad(false).build().also {
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
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
            alpha = alphaInt
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

        // Guard against extreme OOM on huge feet banners while delivering maximum crisp output
        var w = max(1, (baseW * targetScale).toInt())
        var h = max(1, (baseH * targetScale).toInt())
        val maxPixels = 7680L * 4320L // ~33MP cap
        if (w.toLong() * h.toLong() > maxPixels) {
            val factor = kotlin.math.sqrt(maxPixels.toDouble() / (w.toLong() * h.toLong()).toDouble()).toFloat()
            w = max(1, (w * factor).toInt())
            h = max(1, (h * factor).toInt())
        }

        val bmp = try {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            val fallbackW = max(1, w / 2)
            val fallbackH = max(1, h / 2)
            Bitmap.createBitmap(fallbackW, fallbackH, Bitmap.Config.ARGB_8888)
        }
        val actualScaleX = bmp.width.toFloat() / doc.artboardWidth.toFloat()
        val actualScaleY = bmp.height.toFloat() / doc.artboardHeight.toFloat()

        val canvas = Canvas(bmp)
        renderDocument(context, canvas, doc, actualScaleX, actualScaleY, transparentBackground)
        return bmp
    }
}
