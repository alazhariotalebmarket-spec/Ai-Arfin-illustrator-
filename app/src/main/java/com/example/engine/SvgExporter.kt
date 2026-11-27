package com.example.engine

import android.graphics.Color
import android.util.Base64
import com.example.model.*
import java.io.File
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object SvgExporter {

    private fun hexColor(argb: Long): String {
        val c = argb.toInt()
        return String.format("#%02X%02X%02X", Color.red(c), Color.green(c), Color.blue(c))
    }

    private fun alphaValue(argb: Long): Float =
        Color.alpha(argb.toInt()) / 255f

    private fun escapeXml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    fun toSvg(doc: CanvasDocument, transparentBackground: Boolean): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append('\n')
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" width="${doc.artboardWidth}" height="${doc.artboardHeight}" viewBox="0 0 ${doc.artboardWidth} ${doc.artboardHeight}">""").append('\n')

        val defs = StringBuilder()
        val body = StringBuilder()

        if (!transparentBackground) {
            if (doc.backgroundGradientEnd != null) {
                defs.append("""  <linearGradient id="bg_grad" x1="0%" y1="0%" x2="100%" y2="100%">""").append('\n')
                defs.append("""    <stop offset="0%" stop-color="${hexColor(doc.backgroundColor)}" />""").append('\n')
                defs.append("""    <stop offset="100%" stop-color="${hexColor(doc.backgroundGradientEnd)}" />""").append('\n')
                defs.append("""  </linearGradient>""").append('\n')
                body.append("""  <rect width="${doc.artboardWidth}" height="${doc.artboardHeight}" fill="url(#bg_grad)" />""").append('\n')
            } else {
                body.append("""  <rect width="${doc.artboardWidth}" height="${doc.artboardHeight}" fill="${hexColor(doc.backgroundColor)}" />""").append('\n')
            }
        }

        doc.layers.forEachIndexed { idx, layer ->
            if (!layer.visible) return@forEachIndexed
            val cx = layer.x + layer.width / 2f
            val cy = layer.y + layer.height / 2f
            val transform = if (layer.rotation != 0f) {
                """transform="translate(${layer.x}, ${layer.y}) rotate(${layer.rotation}, ${layer.width / 2f}, ${layer.height / 2f})""""
            } else {
                """transform="translate(${layer.x}, ${layer.y})""""
            }
            body.append("""  <g id="${escapeXml(layer.id)}" opacity="${layer.opacity}" $transform>""").append('\n')

            when (layer) {
                is ShapeLayer -> {
                    val fillAttr = if (layer.gradientEndColor != null) {
                        val gradId = "grad_$idx"
                        defs.append("""  <linearGradient id="$gradId" x1="0%" y1="0%" x2="100%" y2="100%">""").append('\n')
                        defs.append("""    <stop offset="0%" stop-color="${hexColor(layer.fillColor)}" />""").append('\n')
                        defs.append("""    <stop offset="100%" stop-color="${hexColor(layer.gradientEndColor)}" />""").append('\n')
                        defs.append("""  </linearGradient>""").append('\n')
                        """fill="url(#$gradId)""""
                    } else if (alphaValue(layer.fillColor) == 0f) {
                        """fill="none""""
                    } else {
                        """fill="${hexColor(layer.fillColor)}" fill-opacity="${alphaValue(layer.fillColor)}""""
                    }
                    val strokeAttr = if (layer.strokeWidth > 0f && alphaValue(layer.strokeColor) > 0f) {
                        """stroke="${hexColor(layer.strokeColor)}" stroke-width="${layer.strokeWidth}""""
                    } else {
                        """stroke="none""""
                    }
                    when (layer.shapeType) {
                        ShapeType.RECTANGLE -> body.append("""    <rect width="${layer.width}" height="${layer.height}" rx="${layer.cornerRadius}" $fillAttr $strokeAttr />""").append('\n')
                        ShapeType.CIRCLE -> body.append("""    <ellipse cx="${layer.width / 2f}" cy="${layer.height / 2f}" rx="${layer.width / 2f}" ry="${layer.height / 2f}" $fillAttr $strokeAttr />""").append('\n')
                        ShapeType.TRIANGLE -> body.append("""    <polygon points="${layer.width / 2f},0 ${layer.width},${layer.height} 0,${layer.height}" $fillAttr $strokeAttr />""").append('\n')
                        ShapeType.LINE -> body.append("""    <line x1="0" y1="${layer.height / 2f}" x2="${layer.width}" y2="${layer.height / 2f}" stroke="${hexColor(layer.fillColor)}" stroke-width="${maxOf(4f, layer.strokeWidth)}" stroke-linecap="round" />""").append('\n')
                        ShapeType.STAR -> {
                            val r = min(layer.width, layer.height) / 2f
                            val pts = buildStarPoints(layer.width / 2f, layer.height / 2f, r, r * 0.45f, 5)
                            body.append("""    <polygon points="$pts" $fillAttr $strokeAttr />""").append('\n')
                        }
                        ShapeType.POLYGON -> {
                            val r = min(layer.width, layer.height) / 2f
                            val pts = buildPolygonPoints(layer.width / 2f, layer.height / 2f, r, layer.polygonSides)
                            body.append("""    <polygon points="$pts" $fillAttr $strokeAttr />""").append('\n')
                        }
                    }
                }
                is PathLayer -> {
                    if (layer.points.isNotEmpty()) {
                        val d = StringBuilder("M ${layer.points[0].x} ${layer.points[0].y}")
                        for (i in 1 until layer.points.size) {
                            d.append(" L ${layer.points[i].x} ${layer.points[i].y}")
                        }
                        if (layer.isClosed) d.append(" Z")
                        val fill = if (layer.fillColor != null) hexColor(layer.fillColor) else "none"
                        body.append("""    <path d="$d" fill="$fill" stroke="${hexColor(layer.strokeColor)}" stroke-width="${layer.strokeWidth}" stroke-linecap="round" stroke-linejoin="round" />""").append('\n')
                    }
                }
                is TextLayer -> {
                    val anchor = when (layer.align) {
                        TextAlignMode.LEFT -> "start"
                        TextAlignMode.CENTER -> "middle"
                        TextAlignMode.RIGHT -> "end"
                    }
                    val tx = when (layer.align) {
                        TextAlignMode.LEFT -> 0f
                        TextAlignMode.CENTER -> layer.width / 2f
                        TextAlignMode.RIGHT -> layer.width
                    }
                    val weight = if (layer.bold) "bold" else "normal"
                    val style = if (layer.italic) "italic" else "normal"
                    layer.text.split("\n").forEachIndexed { lineIdx, line ->
                        val ty = layer.fontSize * (lineIdx + 1)
                        body.append("""    <text x="$tx" y="$ty" font-size="${layer.fontSize}" font-weight="$weight" font-style="$style" text-anchor="$anchor" fill="${hexColor(layer.color)}">${escapeXml(line)}</text>""").append('\n')
                    }
                }
                is ImageLayer -> {
                    val f = File(layer.imagePath)
                    if (f.exists()) {
                        val b64 = Base64.encodeToString(f.readBytes(), Base64.NO_WRAP)
                        body.append("""    <image width="${layer.width}" height="${layer.height}" href="data:image/png;base64,$b64" />""").append('\n')
                    }
                }
                is StickerLayer -> {
                    val asset = StickerCatalog.findById(layer.stickerId)
                    body.append("""    <rect width="${layer.width}" height="${layer.height}" rx="16" fill="${hexColor(layer.primaryColor)}" fill-opacity="0.2" stroke="${hexColor(layer.primaryColor)}" stroke-width="4" />""").append('\n')
                    if (asset.arabicOrLabel.isNotEmpty()) {
                        body.append("""    <text x="${layer.width / 2f}" y="${layer.height / 2f}" text-anchor="middle" fill="${hexColor(layer.primaryColor)}" font-size="${min(layer.width, layer.height) * 0.25f}">${escapeXml(asset.arabicOrLabel)}</text>""").append('\n')
                    }
                }
            }

            body.append("""  </g>""").append('\n')
        }

        if (defs.isNotEmpty()) {
            sb.append("<defs>\n").append(defs).append("</defs>\n")
        }
        sb.append(body)
        sb.append("</svg>")
        return sb.toString()
    }

    private fun buildStarPoints(cx: Float, cy: Float, outerR: Float, innerR: Float, points: Int): String {
        return (0 until points * 2).joinToString(" ") { i ->
            val r = if (i % 2 == 0) outerR else innerR
            val a = -Math.PI / 2.0 + i * Math.PI / points
            "${cx + cos(a) * r},${cy + sin(a) * r}"
        }
    }

    private fun buildPolygonPoints(cx: Float, cy: Float, r: Float, sides: Int): String {
        val safe = maxOf(3, sides)
        return (0 until safe).joinToString(" ") { i ->
            val a = -Math.PI / 2.0 + 2.0 * Math.PI * i / safe
            "${cx + cos(a) * r},${cy + sin(a) * r}"
        }
    }
}
