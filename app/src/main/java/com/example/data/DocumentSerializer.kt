package com.example.data

import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object DocumentSerializer {

    fun toJson(doc: CanvasDocument): String {
        val normalized = doc.normalizedPages()
        val root = JSONObject()
        root.put("id", doc.id)
        root.put("name", doc.name)
        root.put("category", doc.category)
        root.put("artboardWidth", doc.artboardWidth)
        root.put("artboardHeight", doc.artboardHeight)
        root.put("targetExportWidth", doc.targetExportWidth)
        root.put("targetExportHeight", doc.targetExportHeight)
        if (doc.smartModeLabel != null) root.put("smartModeLabel", doc.smartModeLabel)
        root.put("dpi", doc.dpi)
        root.put("backgroundColor", doc.backgroundColor)
        if (doc.backgroundGradientMid != null) root.put("backgroundGradientMid", doc.backgroundGradientMid)
        if (doc.backgroundGradientEnd != null) root.put("backgroundGradientEnd", doc.backgroundGradientEnd)
        root.put("backgroundGradientType", doc.backgroundGradientType.name)
        if (doc.backgroundImagePath != null) root.put("backgroundImagePath", doc.backgroundImagePath)
        root.put("activePageIndex", doc.activePageIndex.coerceIn(0, normalized.lastIndex))
        root.put("updatedAt", doc.updatedAt)
        root.put("layers", encodeLayers(doc.layers))

        val pagesArr = JSONArray()
        normalized.forEach { page ->
            val pObj = JSONObject()
            pObj.put("id", page.id)
            pObj.put("name", page.name)
            pObj.put("artboardWidth", page.artboardWidth)
            pObj.put("artboardHeight", page.artboardHeight)
            pObj.put("targetExportWidth", page.targetExportWidth)
            pObj.put("targetExportHeight", page.targetExportHeight)
            pObj.put("backgroundColor", page.backgroundColor)
            if (page.backgroundGradientMid != null) pObj.put("backgroundGradientMid", page.backgroundGradientMid)
            if (page.backgroundGradientEnd != null) pObj.put("backgroundGradientEnd", page.backgroundGradientEnd)
            pObj.put("backgroundGradientType", page.backgroundGradientType.name)
            if (page.backgroundImagePath != null) pObj.put("backgroundImagePath", page.backgroundImagePath)
            pObj.put("layers", encodeLayers(page.layers))
            pagesArr.put(pObj)
        }
        root.put("pages", pagesArr)
        return root.toString()
    }

    private fun encodeEffects(e: LayerEffects): JSONObject = JSONObject().apply {
        put("shadowEnabled", e.shadowEnabled)
        put("shadowBlur", e.shadowBlur.toDouble())
        put("shadowOffsetX", e.shadowOffsetX.toDouble())
        put("shadowOffsetY", e.shadowOffsetY.toDouble())
        put("shadowColor", e.shadowColor)
        put("glowEnabled", e.glowEnabled)
        put("glowRadius", e.glowRadius.toDouble())
        put("glowColor", e.glowColor)
        put("blurRadius", e.blurRadius.toDouble())
        put("is3dEffect", e.is3dEffect)
        put("outlineWidth", e.outlineWidth.toDouble())
        put("outlineColor", e.outlineColor)
    }

    private fun decodeEffects(obj: JSONObject?): LayerEffects {
        if (obj == null) return LayerEffects()
        return LayerEffects(
            shadowEnabled = obj.optBoolean("shadowEnabled", false),
            shadowBlur = obj.optDouble("shadowBlur", 14.0).toFloat(),
            shadowOffsetX = obj.optDouble("shadowOffsetX", 6.0).toFloat(),
            shadowOffsetY = obj.optDouble("shadowOffsetY", 8.0).toFloat(),
            shadowColor = obj.optLong("shadowColor", 0x88000000L),
            glowEnabled = obj.optBoolean("glowEnabled", false),
            glowRadius = obj.optDouble("glowRadius", 18.0).toFloat(),
            glowColor = obj.optLong("glowColor", 0xFFFF7A00L),
            blurRadius = obj.optDouble("blurRadius", 0.0).toFloat(),
            is3dEffect = obj.optBoolean("is3dEffect", false),
            outlineWidth = obj.optDouble("outlineWidth", 0.0).toFloat(),
            outlineColor = obj.optLong("outlineColor", 0xFFFFFFFFL)
        )
    }

    private fun encodeLayers(layers: List<LayerElement>): JSONArray {
        val layersArr = JSONArray()
        layers.forEach { layer ->
            val obj = JSONObject()
            obj.put("id", layer.id)
            obj.put("name", layer.name)
            obj.put("visible", layer.visible)
            obj.put("locked", layer.locked)
            obj.put("x", layer.x.toDouble())
            obj.put("y", layer.y.toDouble())
            obj.put("width", layer.width.toDouble())
            obj.put("height", layer.height.toDouble())
            obj.put("rotation", layer.rotation.toDouble())
            obj.put("opacity", layer.opacity.toDouble())
            obj.put("flipH", layer.flipH)
            obj.put("flipV", layer.flipV)
            if (layer.groupId != null) obj.put("groupId", layer.groupId)
            obj.put("effects", encodeEffects(layer.effects))

            when (layer) {
                is ShapeLayer -> {
                    obj.put("type", "shape")
                    obj.put("shapeType", layer.shapeType.name)
                    obj.put("fillColor", layer.fillColor)
                    if (layer.gradientMidColor != null) obj.put("gradientMidColor", layer.gradientMidColor)
                    if (layer.gradientEndColor != null) obj.put("gradientEndColor", layer.gradientEndColor)
                    obj.put("gradientAngle", layer.gradientAngle.toDouble())
                    obj.put("gradientType", layer.gradientType.name)
                    obj.put("strokeColor", layer.strokeColor)
                    obj.put("strokeWidth", layer.strokeWidth.toDouble())
                    obj.put("strokeStyle", layer.strokeStyle.name)
                    obj.put("cornerRadius", layer.cornerRadius.toDouble())
                    obj.put("polygonSides", layer.polygonSides)
                    obj.put("arcSweepAngle", layer.arcSweepAngle.toDouble())
                }
                is PathLayer -> {
                    obj.put("type", "path")
                    obj.put("strokeColor", layer.strokeColor)
                    obj.put("strokeWidth", layer.strokeWidth.toDouble())
                    obj.put("strokeStyle", layer.strokeStyle.name)
                    obj.put("isPenSmooth", layer.isPenSmooth)
                    if (layer.fillColor != null) obj.put("fillColor", layer.fillColor)
                    obj.put("isClosed", layer.isClosed)
                    obj.put("brushStyle", layer.brushStyle.name)
                    val ptsArr = JSONArray()
                    layer.points.forEach { pt ->
                        ptsArr.put(JSONObject().apply {
                            put("x", pt.x.toDouble())
                            put("y", pt.y.toDouble())
                        })
                    }
                    obj.put("points", ptsArr)
                }
                is TextLayer -> {
                    obj.put("type", "text")
                    obj.put("text", layer.text)
                    obj.put("fontId", layer.fontId)
                    obj.put("fontSize", layer.fontSize.toDouble())
                    obj.put("color", layer.color)
                    if (layer.gradientMidColor != null) obj.put("gradientMidColor", layer.gradientMidColor)
                    if (layer.gradientEndColor != null) obj.put("gradientEndColor", layer.gradientEndColor)
                    obj.put("gradientType", layer.gradientType.name)
                    obj.put("gradientAngle", layer.gradientAngle.toDouble())
                    obj.put("bold", layer.bold)
                    obj.put("italic", layer.italic)
                    obj.put("underline", layer.underline)
                    obj.put("lineSpacing", layer.lineSpacing.toDouble())
                    obj.put("letterSpacing", layer.letterSpacing.toDouble())
                    if (layer.textBackgroundColor != null) obj.put("textBackgroundColor", layer.textBackgroundColor)
                    obj.put("curveAmount", layer.curveAmount.toDouble())
                    obj.put("align", layer.align.name)
                    obj.put("shadowEnabled", layer.shadowEnabled)
                    obj.put("shadowColor", layer.shadowColor)
                    obj.put("shadowRadius", layer.shadowRadius.toDouble())
                    obj.put("shadowOffsetX", layer.shadowOffsetX.toDouble())
                    obj.put("shadowOffsetY", layer.shadowOffsetY.toDouble())
                    obj.put("strokeColor", layer.strokeColor)
                    obj.put("strokeWidth", layer.strokeWidth.toDouble())
                }
                is ImageLayer -> {
                    obj.put("type", "image")
                    obj.put("imagePath", layer.imagePath)
                    obj.put("originalImagePath", layer.originalImagePath)
                    obj.put("cropLeft", layer.cropLeft.toDouble())
                    obj.put("cropTop", layer.cropTop.toDouble())
                    obj.put("cropRight", layer.cropRight.toDouble())
                    obj.put("cropBottom", layer.cropBottom.toDouble())
                    obj.put("cornerRadiusRatio", layer.cornerRadiusRatio.toDouble())
                }
                is StickerLayer -> {
                    obj.put("type", "sticker")
                    obj.put("stickerId", layer.stickerId)
                    obj.put("primaryColor", layer.primaryColor)
                    obj.put("secondaryColor", layer.secondaryColor)
                }
            }
            layersArr.put(obj)
        }
        return layersArr
    }

    private fun decodeLayers(layersArr: JSONArray): List<LayerElement> {
        val layers = mutableListOf<LayerElement>()
        for (i in 0 until layersArr.length()) {
            val obj = layersArr.getJSONObject(i)
            val id = obj.optString("id", UUID.randomUUID().toString())
            val name = obj.optString("name", "Layer")
            val visible = obj.optBoolean("visible", true)
            val locked = obj.optBoolean("locked", false)
            val x = obj.optDouble("x", 0.0).toFloat()
            val y = obj.optDouble("y", 0.0).toFloat()
            val width = obj.optDouble("width", 200.0).toFloat()
            val height = obj.optDouble("height", 200.0).toFloat()
            val rotation = obj.optDouble("rotation", 0.0).toFloat()
            val opacity = obj.optDouble("opacity", 1.0).toFloat()
            val flipH = obj.optBoolean("flipH", false)
            val flipV = obj.optBoolean("flipV", false)
            val groupId = if (obj.has("groupId")) obj.optString("groupId") else null
            val effects = decodeEffects(obj.optJSONObject("effects"))

            when (obj.optString("type")) {
                "shape" -> {
                    layers.add(
                        ShapeLayer(
                            id = id, name = name, visible = visible, locked = locked,
                            x = x, y = y, width = width, height = height,
                            rotation = rotation, opacity = opacity,
                            shapeType = ShapeType.entries.find { it.name == obj.optString("shapeType") } ?: ShapeType.RECTANGLE,
                            fillColor = obj.optLong("fillColor", 0xFFFF7A00L),
                            gradientMidColor = if (obj.has("gradientMidColor")) obj.optLong("gradientMidColor") else null,
                            gradientEndColor = if (obj.has("gradientEndColor")) obj.optLong("gradientEndColor") else null,
                            gradientAngle = obj.optDouble("gradientAngle", 45.0).toFloat(),
                            gradientType = GradientType.entries.find { it.name == obj.optString("gradientType") } ?: GradientType.LINEAR,
                            strokeColor = obj.optLong("strokeColor", 0L),
                            strokeWidth = obj.optDouble("strokeWidth", 0.0).toFloat(),
                            strokeStyle = StrokeStyle.entries.find { it.name == obj.optString("strokeStyle") } ?: StrokeStyle.SOLID,
                            cornerRadius = obj.optDouble("cornerRadius", 0.0).toFloat(),
                            polygonSides = obj.optInt("polygonSides", 6),
                            arcSweepAngle = obj.optDouble("arcSweepAngle", 260.0).toFloat(),
                            flipH = flipH, flipV = flipV, groupId = groupId, effects = effects
                        )
                    )
                }
                "path" -> {
                    val ptsArr = obj.optJSONArray("points") ?: JSONArray()
                    val pts = mutableListOf<PathPoint>()
                    for (j in 0 until ptsArr.length()) {
                        val pt = ptsArr.getJSONObject(j)
                        pts.add(PathPoint(pt.optDouble("x").toFloat(), pt.optDouble("y").toFloat()))
                    }
                    layers.add(
                        PathLayer(
                            id = id, name = name, visible = visible, locked = locked,
                            x = x, y = y, width = width, height = height,
                            rotation = rotation, opacity = opacity,
                            points = pts,
                            strokeColor = obj.optLong("strokeColor", 0xFF1E1E1EL),
                            strokeWidth = obj.optDouble("strokeWidth", 10.0).toFloat(),
                            strokeStyle = StrokeStyle.entries.find { it.name == obj.optString("strokeStyle") } ?: StrokeStyle.SOLID,
                            isPenSmooth = obj.optBoolean("isPenSmooth", true),
                            fillColor = if (obj.has("fillColor")) obj.optLong("fillColor") else null,
                            isClosed = obj.optBoolean("isClosed", false),
                            brushStyle = BrushStyle.entries.find { it.name == obj.optString("brushStyle") } ?: BrushStyle.ROUND,
                            flipH = flipH, flipV = flipV, groupId = groupId, effects = effects
                        )
                    )
                }
                "text" -> {
                    layers.add(
                        TextLayer(
                            id = id, name = name, visible = visible, locked = locked,
                            x = x, y = y, width = width, height = height,
                            rotation = rotation, opacity = opacity,
                            text = obj.optString("text", "Text"),
                            fontId = obj.optString("fontId", "hind_siliguri"),
                            fontSize = obj.optDouble("fontSize", 64.0).toFloat(),
                            color = obj.optLong("color", 0xFF1E1E1EL),
                            gradientMidColor = if (obj.has("gradientMidColor")) obj.optLong("gradientMidColor") else null,
                            gradientEndColor = if (obj.has("gradientEndColor")) obj.optLong("gradientEndColor") else null,
                            gradientType = GradientType.entries.find { it.name == obj.optString("gradientType") } ?: GradientType.LINEAR,
                            gradientAngle = obj.optDouble("gradientAngle", 0.0).toFloat(),
                            bold = obj.optBoolean("bold", false),
                            italic = obj.optBoolean("italic", false),
                            underline = obj.optBoolean("underline", false),
                            lineSpacing = obj.optDouble("lineSpacing", 1.15).toFloat(),
                            letterSpacing = obj.optDouble("letterSpacing", 0.0).toFloat(),
                            textBackgroundColor = if (obj.has("textBackgroundColor")) obj.optLong("textBackgroundColor") else null,
                            curveAmount = obj.optDouble("curveAmount", 0.0).toFloat(),
                            align = TextAlignMode.entries.find { it.name == obj.optString("align") } ?: TextAlignMode.CENTER,
                            shadowEnabled = obj.optBoolean("shadowEnabled", false),
                            shadowColor = obj.optLong("shadowColor", 0x88000000L),
                            shadowRadius = obj.optDouble("shadowRadius", 8.0).toFloat(),
                            shadowOffsetX = obj.optDouble("shadowOffsetX", 4.0).toFloat(),
                            shadowOffsetY = obj.optDouble("shadowOffsetY", 4.0).toFloat(),
                            strokeColor = obj.optLong("strokeColor", 0xFFFFFFFFL),
                            strokeWidth = obj.optDouble("strokeWidth", 0.0).toFloat(),
                            flipH = flipH, flipV = flipV, groupId = groupId, effects = effects
                        )
                    )
                }
                "image" -> {
                    val imgPath = obj.optString("imagePath", "")
                    layers.add(
                        ImageLayer(
                            id = id, name = name, visible = visible, locked = locked,
                            x = x, y = y, width = width, height = height,
                            rotation = rotation, opacity = opacity,
                            imagePath = imgPath,
                            originalImagePath = obj.optString("originalImagePath", imgPath).ifBlank { imgPath },
                            cropLeft = obj.optDouble("cropLeft", 0.0).toFloat(),
                            cropTop = obj.optDouble("cropTop", 0.0).toFloat(),
                            cropRight = obj.optDouble("cropRight", 1.0).toFloat(),
                            cropBottom = obj.optDouble("cropBottom", 1.0).toFloat(),
                            cornerRadiusRatio = obj.optDouble("cornerRadiusRatio", 0.0).toFloat(),
                            flipH = flipH, flipV = flipV, groupId = groupId, effects = effects
                        )
                    )
                }
                "sticker" -> {
                    layers.add(
                        StickerLayer(
                            id = id, name = name, visible = visible, locked = locked,
                            x = x, y = y, width = width, height = height,
                            rotation = rotation, opacity = opacity,
                            stickerId = obj.optString("stickerId", "isl_crescent"),
                            primaryColor = obj.optLong("primaryColor", 0xFFFF7A00L),
                            secondaryColor = obj.optLong("secondaryColor", 0xFF1E1E1EL),
                            flipH = flipH, flipV = flipV, groupId = groupId, effects = effects
                        )
                    )
                }
            }
        }
        return layers
    }

    fun fromJson(json: String): CanvasDocument {
        val root = JSONObject(json)
        val layers = decodeLayers(root.optJSONArray("layers") ?: JSONArray())
        val artW = root.optInt("artboardWidth", 1080)
        val artH = root.optInt("artboardHeight", 1080)

        val pagesArr = root.optJSONArray("pages")
        val pages = mutableListOf<CanvasPage>()
        if (pagesArr != null) {
            for (i in 0 until pagesArr.length()) {
                val pObj = pagesArr.getJSONObject(i)
                val pw = pObj.optInt("artboardWidth", artW)
                val ph = pObj.optInt("artboardHeight", artH)
                pages.add(
                    CanvasPage(
                        id = pObj.optString("id", UUID.randomUUID().toString()),
                        name = pObj.optString("name", "Page ${i + 1}"),
                        artboardWidth = pw,
                        artboardHeight = ph,
                        targetExportWidth = pObj.optInt("targetExportWidth", pw),
                        targetExportHeight = pObj.optInt("targetExportHeight", ph),
                        backgroundColor = pObj.optLong("backgroundColor", 0xFFFFFFFFL),
                        backgroundGradientMid = if (pObj.has("backgroundGradientMid")) pObj.optLong("backgroundGradientMid") else null,
                        backgroundGradientEnd = if (pObj.has("backgroundGradientEnd")) pObj.optLong("backgroundGradientEnd") else null,
                        backgroundGradientType = GradientType.entries.find { it.name == pObj.optString("backgroundGradientType") } ?: GradientType.LINEAR,
                        backgroundImagePath = if (pObj.has("backgroundImagePath")) pObj.optString("backgroundImagePath") else null,
                        layers = decodeLayers(pObj.optJSONArray("layers") ?: JSONArray())
                    )
                )
            }
        }

        return CanvasDocument(
            id = root.optString("id", UUID.randomUUID().toString()),
            name = root.optString("name", "Untitled Design"),
            category = root.optString("category", "Blank Canvas"),
            artboardWidth = artW,
            artboardHeight = artH,
            targetExportWidth = root.optInt("targetExportWidth", artW),
            targetExportHeight = root.optInt("targetExportHeight", artH),
            smartModeLabel = if (root.has("smartModeLabel")) root.optString("smartModeLabel") else null,
            dpi = root.optInt("dpi", 300),
            backgroundColor = root.optLong("backgroundColor", 0xFFFFFFFFL),
            backgroundGradientMid = if (root.has("backgroundGradientMid")) root.optLong("backgroundGradientMid") else null,
            backgroundGradientEnd = if (root.has("backgroundGradientEnd")) root.optLong("backgroundGradientEnd") else null,
            backgroundGradientType = GradientType.entries.find { it.name == root.optString("backgroundGradientType") } ?: GradientType.LINEAR,
            backgroundImagePath = if (root.has("backgroundImagePath")) root.optString("backgroundImagePath") else null,
            layers = layers,
            pages = pages,
            activePageIndex = root.optInt("activePageIndex", 0),
            updatedAt = root.optLong("updatedAt", System.currentTimeMillis())
        )
    }
}
