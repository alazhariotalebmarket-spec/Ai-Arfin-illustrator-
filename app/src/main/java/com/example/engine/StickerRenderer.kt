package com.example.engine

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import com.example.model.StickerAsset
import com.example.model.StickerCatalog
import com.example.model.StickerRenderKind
import com.example.ui.theme.FontCatalog
import kotlin.math.*

object StickerRenderer {

    fun drawSticker(
        context: Context,
        canvas: Canvas,
        stickerId: String,
        width: Float,
        height: Float,
        primaryColor: Int,
        secondaryColor: Int,
        alpha: Int
    ) {
        val asset = StickerCatalog.findById(stickerId)
        drawAsset(context, canvas, asset, width, height, primaryColor, secondaryColor, alpha)
    }

    fun drawAsset(
        context: Context,
        canvas: Canvas,
        asset: StickerAsset,
        w: Float,
        h: Float,
        primaryColor: Int,
        secondaryColor: Int,
        alpha: Int
    ) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = primaryColor
            this.alpha = alpha
        }
        val secFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = secondaryColor
            this.alpha = alpha
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = primaryColor
            strokeWidth = max(2f, min(w, h) * 0.04f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.alpha = alpha
        }
        val secStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = secondaryColor
            strokeWidth = max(2f, min(w, h) * 0.03f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.alpha = alpha
        }

        val cx = w / 2f
        val cy = h / 2f
        val minDim = min(w, h)

        when (asset.kind) {
            StickerRenderKind.BISMILLAH_SEAL,
            StickerRenderKind.EID_MUBARAK_CREST,
            StickerRenderKind.MASHALLAH_BADGE,
            StickerRenderKind.SUBHANALLAH_CREST,
            StickerRenderKind.ALHAMDULILLAH_CREST,
            StickerRenderKind.JUMMA_MUBARAK_CREST,
            StickerRenderKind.RAMADAN_KAREEM_CREST -> {
                // Ornate double cartouche with Arabic calligraphy text
                val rect = RectF(w * 0.04f, h * 0.1f, w * 0.96f, h * 0.9f)
                secFillPaint.alpha = (alpha * 0.92f).toInt()
                canvas.drawRoundRect(rect, h * 0.4f, h * 0.4f, secFillPaint)
                strokePaint.strokeWidth = max(2f, minDim * 0.045f)
                canvas.drawRoundRect(rect, h * 0.4f, h * 0.4f, strokePaint)
                val inner = RectF(w * 0.08f, h * 0.18f, w * 0.92f, h * 0.82f)
                strokePaint.strokeWidth = max(1f, minDim * 0.02f)
                canvas.drawRoundRect(inner, h * 0.32f, h * 0.32f, strokePaint)

                drawCenteredCalligraphyText(
                    context, canvas, asset.arabicOrLabel, w, h, primaryColor, alpha,
                    scaleFactor = if (asset.arabicOrLabel.length > 14) 0.24f else 0.34f
                )
            }

            StickerRenderKind.ALLAH_MEDALLION,
            StickerRenderKind.MUHAMMAD_MEDALLION -> {
                val r = minDim * 0.45f
                canvas.drawCircle(cx, cy, r, secFillPaint)
                strokePaint.strokeWidth = minDim * 0.05f
                canvas.drawCircle(cx, cy, r * 0.94f, strokePaint)
                drawStarPolygon(canvas, cx, cy, r * 0.86f, r * 0.75f, 12, strokePaint)
                drawCenteredCalligraphyText(context, canvas, asset.arabicOrLabel, w, h, primaryColor, alpha, 0.38f)
            }

            StickerRenderKind.CRESCENT_STAR -> {
                val r = minDim * 0.38f
                val path = Path().apply {
                    addCircle(cx - r * 0.08f, cy, r, Path.Direction.CW)
                }
                val cut = Path().apply {
                    addCircle(cx + r * 0.28f, cy - r * 0.12f, r * 0.8f, Path.Direction.CW)
                }
                path.op(cut, Path.Op.DIFFERENCE)
                canvas.drawPath(path, fillPaint)
                drawStarPolygon(canvas, cx + r * 0.48f, cy - r * 0.32f, r * 0.28f, r * 0.12f, 5, fillPaint)
            }

            StickerRenderKind.MOSQUE_SILHOUETTE,
            StickerRenderKind.DOME_MINARET -> {
                // Base building
                canvas.drawRect(w * 0.14f, h * 0.56f, w * 0.86f, h * 0.92f, fillPaint)
                // Central Dome
                val domePath = Path().apply {
                    moveTo(w * 0.28f, h * 0.56f)
                    cubicTo(w * 0.28f, h * 0.24f, w * 0.5f, h * 0.18f, w * 0.5f, h * 0.14f)
                    cubicTo(w * 0.5f, h * 0.18f, w * 0.72f, h * 0.24f, w * 0.72f, h * 0.56f)
                    close()
                }
                canvas.drawPath(domePath, fillPaint)
                // Side Minarets
                canvas.drawRect(w * 0.08f, h * 0.28f, w * 0.16f, h * 0.92f, fillPaint)
                canvas.drawRect(w * 0.84f, h * 0.28f, w * 0.92f, h * 0.92f, fillPaint)
                // Minaret tops
                val leftTop = Path().apply {
                    moveTo(w * 0.06f, h * 0.28f)
                    lineTo(w * 0.12f, h * 0.14f)
                    lineTo(w * 0.18f, h * 0.28f)
                    close()
                }
                val rightTop = Path().apply {
                    moveTo(w * 0.82f, h * 0.28f)
                    lineTo(w * 0.88f, h * 0.14f)
                    lineTo(w * 0.94f, h * 0.28f)
                    close()
                }
                canvas.drawPath(leftTop, fillPaint)
                canvas.drawPath(rightTop, fillPaint)
                // Door arch
                val door = Path().apply {
                    moveTo(w * 0.42f, h * 0.92f)
                    lineTo(w * 0.42f, h * 0.72f)
                    quadTo(w * 0.5f, h * 0.62f, w * 0.58f, h * 0.72f)
                    lineTo(w * 0.58f, h * 0.92f)
                    close()
                }
                canvas.drawPath(door, secFillPaint)
            }

            StickerRenderKind.MIHRAB_ARCH,
            StickerRenderKind.FRAME_ORNATE_ARCH -> {
                val outer = Path().apply {
                    moveTo(w * 0.08f, h * 0.94f)
                    lineTo(w * 0.08f, h * 0.36f)
                    cubicTo(w * 0.08f, h * 0.18f, w * 0.35f, h * 0.12f, w * 0.5f, h * 0.05f)
                    cubicTo(w * 0.65f, h * 0.12f, w * 0.92f, h * 0.18f, w * 0.92f, h * 0.36f)
                    lineTo(w * 0.92f, h * 0.94f)
                    close()
                }
                strokePaint.strokeWidth = minDim * 0.03f
                canvas.drawPath(outer, strokePaint)
                val inner = Path().apply {
                    moveTo(w * 0.14f, h * 0.9f)
                    lineTo(w * 0.14f, h * 0.38f)
                    cubicTo(w * 0.14f, h * 0.23f, w * 0.37f, h * 0.17f, w * 0.5f, h * 0.11f)
                    cubicTo(w * 0.63f, h * 0.17f, w * 0.86f, h * 0.23f, w * 0.86f, h * 0.38f)
                    lineTo(w * 0.86f, h * 0.9f)
                    close()
                }
                strokePaint.strokeWidth = minDim * 0.014f
                canvas.drawPath(inner, strokePaint)
            }

            StickerRenderKind.LANTERN_FANOOS -> {
                // Hanging chain
                strokePaint.strokeWidth = minDim * 0.03f
                canvas.drawLine(cx, 0f, cx, h * 0.18f, strokePaint)
                canvas.drawCircle(cx, h * 0.21f, minDim * 0.05f, strokePaint)
                // Top dome
                val top = Path().apply {
                    moveTo(w * 0.32f, h * 0.34f)
                    quadTo(cx, h * 0.20f, w * 0.68f, h * 0.34f)
                    close()
                }
                canvas.drawPath(top, fillPaint)
                // Glass body
                val body = Path().apply {
                    moveTo(w * 0.28f, h * 0.34f)
                    lineTo(w * 0.72f, h * 0.34f)
                    lineTo(w * 0.64f, h * 0.76f)
                    lineTo(w * 0.36f, h * 0.76f)
                    close()
                }
                canvas.drawPath(body, secFillPaint)
                canvas.drawPath(body, strokePaint)
                // Inner warm glow
                canvas.drawOval(RectF(w * 0.41f, h * 0.44f, w * 0.59f, h * 0.66f), fillPaint)
                // Bottom finial
                val bot = Path().apply {
                    moveTo(w * 0.34f, h * 0.76f)
                    lineTo(w * 0.66f, h * 0.76f)
                    lineTo(cx, h * 0.94f)
                    close()
                }
                canvas.drawPath(bot, fillPaint)
            }

            StickerRenderKind.ISLAMIC_8_STAR -> {
                val r = minDim * 0.34f
                canvas.save()
                canvas.drawRect(cx - r, cy - r, cx + r, cy + r, strokePaint)
                canvas.rotate(45f, cx, cy)
                canvas.drawRect(cx - r, cy - r, cx + r, cy + r, strokePaint)
                canvas.restore()
                canvas.drawCircle(cx, cy, r * 0.42f, fillPaint)
            }

            StickerRenderKind.ISLAMIC_12_STAR -> {
                drawStarPolygon(canvas, cx, cy, minDim * 0.46f, minDim * 0.26f, 12, fillPaint)
                canvas.drawCircle(cx, cy, minDim * 0.16f, secFillPaint)
            }

            StickerRenderKind.ARABESQUE_CORNER -> {
                strokePaint.strokeWidth = minDim * 0.045f
                canvas.drawLine(w * 0.08f, h * 0.08f, w * 0.9f, h * 0.08f, strokePaint)
                canvas.drawLine(w * 0.08f, h * 0.08f, w * 0.08f, h * 0.9f, strokePaint)
                val arc = Path().apply {
                    moveTo(w * 0.08f, h * 0.55f)
                    cubicTo(w * 0.35f, h * 0.55f, w * 0.55f, h * 0.35f, w * 0.55f, h * 0.08f)
                }
                canvas.drawPath(arc, strokePaint)
                canvas.drawCircle(w * 0.24f, h * 0.24f, minDim * 0.08f, fillPaint)
            }

            StickerRenderKind.TASBIH_WREATH,
            StickerRenderKind.FRAME_CIRCLE_WREATH -> {
                val r = minDim * 0.42f
                val count = 24
                for (i in 0 until count) {
                    val angle = (2 * PI * i / count).toFloat()
                    val bx = cx + cos(angle) * r
                    val by = cy + sin(angle) * r
                    val p = if (i % 2 == 0) fillPaint else secFillPaint
                    canvas.drawCircle(bx, by, minDim * 0.036f, p)
                }
            }

            StickerRenderKind.QURAN_STAND -> {
                strokePaint.strokeWidth = minDim * 0.06f
                canvas.drawLine(w * 0.2f, h * 0.82f, w * 0.78f, h * 0.28f, strokePaint)
                canvas.drawLine(w * 0.8f, h * 0.82f, w * 0.22f, h * 0.28f, strokePaint)
                val book = Path().apply {
                    moveTo(w * 0.22f, h * 0.24f)
                    quadTo(cx, h * 0.38f, w * 0.78f, h * 0.24f)
                }
                secStrokePaint.strokeWidth = minDim * 0.05f
                canvas.drawPath(book, secStrokePaint)
            }

            StickerRenderKind.PALM_CRESCENT -> {
                val r = minDim * 0.3f
                canvas.drawCircle(cx, cy - h * 0.08f, r, fillPaint)
                strokePaint.strokeWidth = minDim * 0.045f
                canvas.drawLine(cx, cy + r * 0.5f, cx, h * 0.9f, strokePaint)
            }

            StickerRenderKind.FRAME_DOUBLE_GOLD -> {
                strokePaint.strokeWidth = minDim * 0.024f
                canvas.drawRect(w * 0.03f, h * 0.03f, w * 0.97f, h * 0.97f, strokePaint)
                strokePaint.strokeWidth = minDim * 0.012f
                canvas.drawRect(w * 0.065f, h * 0.065f, w * 0.935f, h * 0.935f, strokePaint)
                val cornerSize = minDim * 0.05f
                listOf(
                    w * 0.03f to h * 0.03f,
                    w * 0.97f - cornerSize to h * 0.03f,
                    w * 0.03f to h * 0.97f - cornerSize,
                    w * 0.97f - cornerSize to h * 0.97f - cornerSize
                ).forEach { (rx, ry) ->
                    canvas.drawRect(rx, ry, rx + cornerSize, ry + cornerSize, fillPaint)
                }
            }

            StickerRenderKind.FRAME_HEXAGON_TECH -> {
                drawRegularPolygon(canvas, cx, cy, minDim * 0.44f, 6, strokePaint)
                drawRegularPolygon(canvas, cx, cy, minDim * 0.38f, 6, secStrokePaint)
            }

            StickerRenderKind.FRAME_DASHED_TICKET -> {
                val dashPaint = Paint(strokePaint).apply {
                    pathEffect = DashPathEffect(floatArrayOf(minDim * 0.06f, minDim * 0.04f), 0f)
                }
                canvas.drawRoundRect(
                    RectF(w * 0.04f, h * 0.04f, w * 0.96f, h * 0.96f),
                    minDim * 0.08f, minDim * 0.08f, dashPaint
                )
            }

            StickerRenderKind.FRAME_CORNER_BRACKETS -> {
                val len = minDim * 0.22f
                strokePaint.strokeWidth = minDim * 0.045f
                // TL
                canvas.drawLine(w * 0.06f, h * 0.06f, w * 0.06f + len, h * 0.06f, strokePaint)
                canvas.drawLine(w * 0.06f, h * 0.06f, w * 0.06f, h * 0.06f + len, strokePaint)
                // TR
                canvas.drawLine(w * 0.94f, h * 0.06f, w * 0.94f - len, h * 0.06f, strokePaint)
                canvas.drawLine(w * 0.94f, h * 0.06f, w * 0.94f, h * 0.06f + len, strokePaint)
                // BL
                canvas.drawLine(w * 0.06f, h * 0.94f, w * 0.06f + len, h * 0.94f, strokePaint)
                canvas.drawLine(w * 0.06f, h * 0.94f, w * 0.06f, h * 0.94f - len, strokePaint)
                // BR
                canvas.drawLine(w * 0.94f, h * 0.94f, w * 0.94f - len, h * 0.94f, strokePaint)
                canvas.drawLine(w * 0.94f, h * 0.94f, w * 0.94f, h * 0.94f - len, strokePaint)
            }

            StickerRenderKind.FRAME_RIBBON_HEADER,
            StickerRenderKind.FRAME_POLAROID,
            StickerRenderKind.FRAME_ROUNDED_NEON,
            StickerRenderKind.FRAME_BRUSH_BOX,
            StickerRenderKind.FRAME_STAMP_EDGE -> {
                strokePaint.strokeWidth = minDim * 0.04f
                canvas.drawRoundRect(
                    RectF(w * 0.05f, h * 0.05f, w * 0.95f, h * 0.95f),
                    minDim * 0.1f, minDim * 0.1f, strokePaint
                )
            }

            StickerRenderKind.FRAME_DIAMOND_ROYAL -> {
                val p = Path().apply {
                    moveTo(cx, h * 0.04f)
                    lineTo(w * 0.96f, cy)
                    lineTo(cx, h * 0.96f)
                    lineTo(w * 0.04f, cy)
                    close()
                }
                canvas.drawPath(p, strokePaint)
            }

            StickerRenderKind.BADGE_SEAL_12,
            StickerRenderKind.BADGE_SEAL_24,
            StickerRenderKind.BADGE_OFFER_BURST -> {
                val pts = if (asset.kind == StickerRenderKind.BADGE_SEAL_12) 12 else 20
                drawStarPolygon(canvas, cx, cy, minDim * 0.46f, minDim * 0.36f, pts, fillPaint)
                if (asset.arabicOrLabel.isNotEmpty()) {
                    drawCenteredCalligraphyText(context, canvas, asset.arabicOrLabel, w, h, secondaryColor, alpha, 0.32f)
                }
            }

            StickerRenderKind.BADGE_SHIELD -> {
                val shield = Path().apply {
                    moveTo(w * 0.16f, h * 0.12f)
                    lineTo(w * 0.84f, h * 0.12f)
                    lineTo(w * 0.84f, h * 0.52f)
                    cubicTo(w * 0.84f, h * 0.76f, cx, h * 0.92f, cx, h * 0.92f)
                    cubicTo(cx, h * 0.92f, w * 0.16f, h * 0.76f, w * 0.16f, h * 0.52f)
                    close()
                }
                canvas.drawPath(shield, fillPaint)
                canvas.drawPath(shield, secStrokePaint)
            }

            StickerRenderKind.BADGE_CROWN,
            StickerRenderKind.NAT_SPARK_CROWN -> {
                val crown = Path().apply {
                    moveTo(w * 0.12f, h * 0.75f)
                    lineTo(w * 0.18f, h * 0.32f)
                    lineTo(w * 0.36f, h * 0.55f)
                    lineTo(cx, h * 0.22f)
                    lineTo(w * 0.64f, h * 0.55f)
                    lineTo(w * 0.82f, h * 0.32f)
                    lineTo(w * 0.88f, h * 0.75f)
                    close()
                }
                canvas.drawPath(crown, fillPaint)
                canvas.drawRect(w * 0.12f, h * 0.78f, w * 0.88f, h * 0.86f, fillPaint)
            }

            StickerRenderKind.BADGE_TROPHY -> {
                canvas.drawArc(RectF(w * 0.26f, h * 0.16f, w * 0.74f, h * 0.64f), 0f, 180f, true, fillPaint)
                canvas.drawRect(w * 0.44f, h * 0.62f, w * 0.56f, h * 0.8f, fillPaint)
                canvas.drawRoundRect(RectF(w * 0.3f, h * 0.8f, w * 0.7f, h * 0.9f), 8f, 8f, fillPaint)
            }

            StickerRenderKind.BADGE_VERIFIED,
            StickerRenderKind.SOC_VERIFIED_CHECK -> {
                drawStarPolygon(canvas, cx, cy, minDim * 0.44f, minDim * 0.36f, 10, fillPaint)
                secStrokePaint.strokeWidth = minDim * 0.08f
                val check = Path().apply {
                    moveTo(w * 0.32f, cy)
                    lineTo(w * 0.45f, h * 0.64f)
                    lineTo(w * 0.7f, h * 0.36f)
                }
                canvas.drawPath(check, secStrokePaint)
            }

            StickerRenderKind.BADGE_NEW_TAG,
            StickerRenderKind.SOC_SUBSCRIBE_PILL,
            StickerRenderKind.SOC_LIVE_DOT -> {
                canvas.drawRoundRect(RectF(w * 0.04f, h * 0.18f, w * 0.96f, h * 0.82f), h * 0.32f, h * 0.32f, fillPaint)
                drawCenteredCalligraphyText(context, canvas, asset.arabicOrLabel, w, h, secondaryColor, alpha, 0.34f)
            }

            StickerRenderKind.RIBBON_BANNER -> {
                val p = Path().apply {
                    moveTo(w * 0.04f, h * 0.25f)
                    lineTo(w * 0.96f, h * 0.25f)
                    lineTo(w * 0.88f, cy)
                    lineTo(w * 0.96f, h * 0.75f)
                    lineTo(w * 0.04f, h * 0.75f)
                    lineTo(w * 0.12f, cy)
                    close()
                }
                canvas.drawPath(p, fillPaint)
                if (asset.arabicOrLabel.isNotEmpty()) {
                    drawCenteredCalligraphyText(context, canvas, asset.arabicOrLabel, w, h, secondaryColor, alpha, 0.28f)
                }
            }

            StickerRenderKind.BADGE_HOT_FLAME -> {
                val flame = Path().apply {
                    moveTo(cx, h * 0.08f)
                    cubicTo(w * 0.84f, h * 0.42f, w * 0.82f, h * 0.88f, cx, h * 0.92f)
                    cubicTo(w * 0.18f, h * 0.88f, w * 0.16f, h * 0.42f, cx, h * 0.08f)
                    close()
                }
                canvas.drawPath(flame, fillPaint)
            }

            StickerRenderKind.RIBBON_AWARD,
            StickerRenderKind.BADGE_STAR_MEDAL -> {
                canvas.drawCircle(cx, cy * 0.85f, minDim * 0.32f, fillPaint)
                val leg1 = Path().apply {
                    moveTo(w * 0.34f, h * 0.62f)
                    lineTo(w * 0.26f, h * 0.94f)
                    lineTo(w * 0.44f, h * 0.84f)
                    close()
                }
                val leg2 = Path().apply {
                    moveTo(w * 0.66f, h * 0.62f)
                    lineTo(w * 0.74f, h * 0.94f)
                    lineTo(w * 0.56f, h * 0.84f)
                    close()
                }
                canvas.drawPath(leg1, secFillPaint)
                canvas.drawPath(leg2, secFillPaint)
            }

            StickerRenderKind.RIBBON_BOOKMARK -> {
                val bm = Path().apply {
                    moveTo(w * 0.24f, h * 0.08f)
                    lineTo(w * 0.76f, h * 0.08f)
                    lineTo(w * 0.76f, h * 0.92f)
                    lineTo(cx, h * 0.72f)
                    lineTo(w * 0.24f, h * 0.92f)
                    close()
                }
                canvas.drawPath(bm, fillPaint)
            }

            StickerRenderKind.RIBBON_CORNER_FOLD,
            StickerRenderKind.BADGE_DIAMOND_VIP -> {
                val dia = Path().apply {
                    moveTo(cx, h * 0.12f)
                    lineTo(w * 0.88f, cy)
                    lineTo(cx, h * 0.88f)
                    lineTo(w * 0.12f, cy)
                    close()
                }
                canvas.drawPath(dia, fillPaint)
                if (asset.arabicOrLabel.isNotEmpty()) {
                    drawCenteredCalligraphyText(context, canvas, asset.arabicOrLabel, w, h, secondaryColor, alpha, 0.32f)
                }
            }

            StickerRenderKind.GEO_HEX_GRID -> {
                drawRegularPolygon(canvas, w * 0.35f, h * 0.35f, minDim * 0.22f, 6, fillPaint)
                drawRegularPolygon(canvas, w * 0.68f, h * 0.45f, minDim * 0.2f, 6, secFillPaint)
                drawRegularPolygon(canvas, w * 0.42f, h * 0.68f, minDim * 0.18f, 6, strokePaint)
            }

            StickerRenderKind.GEO_CONCENTRIC -> {
                strokePaint.strokeWidth = minDim * 0.04f
                canvas.drawCircle(cx, cy, minDim * 0.44f, strokePaint)
                canvas.drawCircle(cx, cy, minDim * 0.31f, secStrokePaint)
                canvas.drawCircle(cx, cy, minDim * 0.16f, fillPaint)
            }

            StickerRenderKind.GEO_SUNBURST,
            StickerRenderKind.NAT_FIREWORK -> {
                strokePaint.strokeWidth = minDim * 0.035f
                val rays = 16
                for (i in 0 until rays) {
                    val a = (2 * PI * i / rays).toFloat()
                    canvas.drawLine(
                        cx + cos(a) * minDim * 0.18f,
                        cy + sin(a) * minDim * 0.18f,
                        cx + cos(a) * minDim * 0.46f,
                        cy + sin(a) * minDim * 0.46f,
                        if (i % 2 == 0) strokePaint else secStrokePaint
                    )
                }
            }

            StickerRenderKind.GEO_GRID_DOTS,
            StickerRenderKind.GEO_HALFTONE -> {
                val cols = 4
                val stepX = w / (cols + 1)
                val stepY = h / (cols + 1)
                for (r in 1..cols) {
                    for (c in 1..cols) {
                        val radius = minDim * (0.03f + 0.015f * (r / cols.toFloat()))
                        canvas.drawCircle(c * stepX, r * stepY, radius, fillPaint)
                    }
                }
            }

            StickerRenderKind.GEO_SPARKLE_4,
            StickerRenderKind.GEO_SPARKLE_GROUP -> {
                drawStarPolygon(canvas, cx, cy, minDim * 0.46f, minDim * 0.11f, 4, fillPaint)
                if (asset.kind == StickerRenderKind.GEO_SPARKLE_GROUP) {
                    drawStarPolygon(canvas, w * 0.22f, h * 0.25f, minDim * 0.18f, minDim * 0.04f, 4, secFillPaint)
                    drawStarPolygon(canvas, w * 0.78f, h * 0.75f, minDim * 0.16f, minDim * 0.04f, 4, secFillPaint)
                }
            }

            StickerRenderKind.GEO_WAVE_BARS -> {
                val heights = listOf(0.3f, 0.65f, 0.9f, 0.5f, 0.8f, 0.4f)
                val barW = w / (heights.size * 2f)
                heights.forEachIndexed { idx, hf ->
                    val bx = (idx * 2 + 0.5f) * barW
                    val bh = h * hf
                    canvas.drawRoundRect(
                        RectF(bx, cy - bh / 2f, bx + barW, cy + bh / 2f),
                        barW / 2f, barW / 2f, fillPaint
                    )
                }
            }

            StickerRenderKind.GEO_ABSTRACT_BLOB -> {
                val blob = Path().apply {
                    moveTo(w * 0.24f, h * 0.26f)
                    cubicTo(w * 0.65f, h * 0.04f, w * 0.96f, h * 0.28f, w * 0.86f, h * 0.64f)
                    cubicTo(w * 0.76f, h * 0.96f, w * 0.28f, h * 0.94f, w * 0.14f, h * 0.68f)
                    cubicTo(w * 0.04f, h * 0.46f, w * 0.1f, h * 0.34f, w * 0.24f, h * 0.26f)
                    close()
                }
                canvas.drawPath(blob, fillPaint)
            }

            StickerRenderKind.GEO_SPIRAL,
            StickerRenderKind.GEO_TRIANGLE_STACK,
            StickerRenderKind.GEO_PILL_CLUSTER,
            StickerRenderKind.GEO_CHECKER_ACCENT,
            StickerRenderKind.GEO_DIAGONAL_STRIPES,
            StickerRenderKind.GEO_ZIGZAG,
            StickerRenderKind.GEO_CROSSHAIR -> {
                strokePaint.strokeWidth = minDim * 0.045f
                canvas.drawCircle(cx, cy, minDim * 0.36f, strokePaint)
                canvas.drawLine(w * 0.1f, cy, w * 0.9f, cy, secStrokePaint)
                canvas.drawLine(cx, h * 0.1f, cx, h * 0.9f, secStrokePaint)
            }

            StickerRenderKind.SOC_PLAY_BUTTON -> {
                canvas.drawRoundRect(
                    RectF(w * 0.08f, h * 0.2f, w * 0.92f, h * 0.8f),
                    minDim * 0.18f, minDim * 0.18f, fillPaint
                )
                val tri = Path().apply {
                    moveTo(w * 0.4f, h * 0.34f)
                    lineTo(w * 0.68f, cy)
                    lineTo(w * 0.4f, h * 0.66f)
                    close()
                }
                canvas.drawPath(tri, secFillPaint)
            }

            StickerRenderKind.SOC_HEART_LIKE,
            StickerRenderKind.NAT_HEART_DOUBLE -> {
                val heart = Path().apply {
                    moveTo(cx, h * 0.84f)
                    cubicTo(w * 0.1f, h * 0.58f, w * 0.08f, h * 0.22f, w * 0.32f, h * 0.22f)
                    cubicTo(w * 0.44f, h * 0.22f, cx, h * 0.34f, cx, h * 0.36f)
                    cubicTo(cx, h * 0.34f, w * 0.56f, h * 0.22f, w * 0.68f, h * 0.22f)
                    cubicTo(w * 0.92f, h * 0.22f, w * 0.9f, h * 0.58f, cx, h * 0.84f)
                    close()
                }
                canvas.drawPath(heart, fillPaint)
            }

            StickerRenderKind.SOC_LOCATION_PIN -> {
                val pin = Path().apply {
                    addCircle(cx, h * 0.36f, minDim * 0.28f, Path.Direction.CW)
                }
                canvas.drawPath(pin, fillPaint)
                val tail = Path().apply {
                    moveTo(cx - minDim * 0.22f, h * 0.48f)
                    lineTo(cx + minDim * 0.22f, h * 0.48f)
                    lineTo(cx, h * 0.92f)
                    close()
                }
                canvas.drawPath(tail, fillPaint)
                canvas.drawCircle(cx, h * 0.36f, minDim * 0.11f, secFillPaint)
            }

            StickerRenderKind.SOC_BELL_RING,
            StickerRenderKind.SOC_THUMBS_UP,
            StickerRenderKind.SOC_SHARE_NODES,
            StickerRenderKind.SOC_COMMENT_BUBBLE,
            StickerRenderKind.SOC_CAMERA_LENS,
            StickerRenderKind.SOC_MIC_PODCAST,
            StickerRenderKind.SOC_PHONE_CALL,
            StickerRenderKind.SOC_GLOBE_WEB,
            StickerRenderKind.SOC_AT_HANDLE -> {
                canvas.drawCircle(cx, cy, minDim * 0.44f, fillPaint)
                canvas.drawCircle(cx, cy, minDim * 0.34f, secStrokePaint)
                val txt = when (asset.kind) {
                    StickerRenderKind.SOC_AT_HANDLE -> "@"
                    StickerRenderKind.SOC_PHONE_CALL -> "☎"
                    StickerRenderKind.SOC_GLOBE_WEB -> "WWW"
                    StickerRenderKind.SOC_COMMENT_BUBBLE -> "..."
                    else -> "★"
                }
                drawCenteredCalligraphyText(context, canvas, txt, w, h, secondaryColor, alpha, 0.36f)
            }

            StickerRenderKind.ARR_RIGHT_BOLD,
            StickerRenderKind.ARR_CURVED_SWOOSH,
            StickerRenderKind.ARR_DOUBLE_CHEVRON,
            StickerRenderKind.ARR_CIRCULAR_LOOP,
            StickerRenderKind.ARR_UP_GROWTH,
            StickerRenderKind.ARR_DOWN_DOWNLOAD,
            StickerRenderKind.ARR_DIAGONAL_POINTER,
            StickerRenderKind.ARR_HAND_UNDERLINE,
            StickerRenderKind.ARR_BRACKET_LEFT,
            StickerRenderKind.ARR_BRACKET_RIGHT,
            StickerRenderKind.ARR_SPEECH_POINTER -> {
                val arrow = Path().apply {
                    moveTo(w * 0.1f, h * 0.38f)
                    lineTo(w * 0.58f, h * 0.38f)
                    lineTo(w * 0.58f, h * 0.18f)
                    lineTo(w * 0.92f, cy)
                    lineTo(w * 0.58f, h * 0.82f)
                    lineTo(w * 0.58f, h * 0.62f)
                    lineTo(w * 0.1f, h * 0.62f)
                    close()
                }
                canvas.drawPath(arrow, fillPaint)
            }

            StickerRenderKind.ARR_LIGHTNING_BOLT -> {
                val bolt = Path().apply {
                    moveTo(w * 0.56f, h * 0.06f)
                    lineTo(w * 0.24f, h * 0.54f)
                    lineTo(w * 0.48f, h * 0.54f)
                    lineTo(w * 0.4f, h * 0.94f)
                    lineTo(w * 0.78f, h * 0.44f)
                    lineTo(w * 0.54f, h * 0.44f)
                    close()
                }
                canvas.drawPath(bolt, fillPaint)
            }

            StickerRenderKind.NAT_PEN_NIB -> {
                val nib = Path().apply {
                    moveTo(cx, h * 0.08f)
                    lineTo(w * 0.82f, h * 0.56f)
                    lineTo(w * 0.68f, h * 0.88f)
                    lineTo(w * 0.32f, h * 0.88f)
                    lineTo(w * 0.18f, h * 0.56f)
                    close()
                }
                canvas.drawPath(nib, fillPaint)
                secStrokePaint.strokeWidth = minDim * 0.04f
                canvas.drawLine(cx, h * 0.08f, cx, h * 0.54f, secStrokePaint)
                canvas.drawCircle(cx, h * 0.54f, minDim * 0.07f, secFillPaint)
            }

            StickerRenderKind.NAT_FLOWER_LOTUS,
            StickerRenderKind.NAT_LEAF_BRANCH,
            StickerRenderKind.NAT_SUN_RAYS,
            StickerRenderKind.NAT_MOON_CLOUDS,
            StickerRenderKind.NAT_CONFETTI_BURST,
            StickerRenderKind.NAT_GIFT_BOX,
            StickerRenderKind.NAT_BUTTERFLY,
            StickerRenderKind.NAT_FEATHER_QUILL,
            StickerRenderKind.NAT_MOUNTAIN_PEAK,
            StickerRenderKind.NAT_OCEAN_WAVE,
            StickerRenderKind.NAT_MUSIC_NOTE,
            StickerRenderKind.NAT_PALETTE_ART -> {
                // Stylized 6-petal rosette / emblem
                for (i in 0 until 6) {
                    canvas.save()
                    canvas.rotate(i * 60f, cx, cy)
                    canvas.drawOval(RectF(cx - minDim * 0.14f, h * 0.12f, cx + minDim * 0.14f, cy), fillPaint)
                    canvas.restore()
                }
                canvas.drawCircle(cx, cy, minDim * 0.14f, secFillPaint)
            }

            StickerRenderKind.BD_FLAG_CREST -> {
                val outerR = minDim * 0.45f
                canvas.drawCircle(cx, cy, outerR, fillPaint)
                canvas.drawCircle(cx - minDim * 0.03f, cy, minDim * 0.21f, secFillPaint)
                secStrokePaint.strokeWidth = minDim * 0.025f
                canvas.drawCircle(cx, cy, outerR * 0.92f, secStrokePaint)
            }

            StickerRenderKind.BD_SHAPLA_FLOWER -> {
                val angles = listOf(-48f, -24f, 0f, 24f, 48f)
                angles.forEach { deg ->
                    canvas.save()
                    canvas.rotate(deg, cx, h * 0.8f)
                    val petal = Path().apply {
                        moveTo(cx, h * 0.78f)
                        cubicTo(cx - minDim * 0.15f, h * 0.52f, cx - minDim * 0.11f, h * 0.24f, cx, h * 0.12f)
                        cubicTo(cx + minDim * 0.11f, h * 0.24f, cx + minDim * 0.15f, h * 0.52f, cx, h * 0.78f)
                        close()
                    }
                    canvas.drawPath(petal, fillPaint)
                    secStrokePaint.strokeWidth = minDim * 0.018f
                    canvas.drawPath(petal, secStrokePaint)
                    canvas.restore()
                }
                canvas.drawRoundRect(RectF(w * 0.2f, h * 0.78f, w * 0.8f, h * 0.87f), 12f, 12f, secFillPaint)
            }

            StickerRenderKind.BD_SEAL_TEXT -> {
                drawStarPolygon(canvas, cx, cy, minDim * 0.46f, minDim * 0.37f, 18, fillPaint)
                secStrokePaint.strokeWidth = minDim * 0.025f
                canvas.drawCircle(cx, cy, minDim * 0.32f, secStrokePaint)
                drawCenteredCalligraphyText(
                    context = context,
                    canvas = canvas,
                    text = asset.arabicOrLabel,
                    w = w,
                    h = h,
                    color = secondaryColor,
                    alpha = alpha,
                    scaleFactor = if (asset.arabicOrLabel.length > 8) 0.16f else 0.21f
                )
            }
        }
    }

    private fun drawCenteredCalligraphyText(
        context: Context,
        canvas: Canvas,
        text: String,
        w: Float,
        h: Float,
        color: Int,
        alpha: Int,
        scaleFactor: Float
    ) {
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.alpha = alpha
            textSize = min(w, h) * scaleFactor
            textAlign = Paint.Align.CENTER
            typeface = FontCatalog.getTypeface(context, FontCatalog.findById("noto_serif_bn_bold"), bold = true, italic = false)
        }
        val fm = tp.fontMetrics
        val baseline = h / 2f - (fm.ascent + fm.descent) / 2f
        canvas.drawText(text, w / 2f, baseline, tp)
    }

    fun drawStarPolygon(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        outerR: Float,
        innerR: Float,
        points: Int,
        paint: Paint
    ) {
        val path = Path()
        val total = points * 2
        for (i in 0 until total) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (-PI / 2.0 + i * PI / points).toFloat()
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    fun drawRegularPolygon(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        sides: Int,
        paint: Paint
    ) {
        val path = Path()
        val safeSides = max(3, sides)
        for (i in 0 until safeSides) {
            val angle = (-PI / 2.0 + 2.0 * PI * i / safeSides).toFloat()
            val x = cx + cos(angle) * radius
            val y = cy + sin(angle) * radius
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }
}
