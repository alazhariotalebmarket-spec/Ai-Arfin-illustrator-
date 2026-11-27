package com.example.data

import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object DocumentSerializer {

    fun toJson(doc: CanvasDocument): String {
        val root = JSONObject()
        root.put("id", doc.id)
        root.put("name", doc.name)
        root.put("category", doc.category)
        root.put("artboardWidth", doc.artboardWidth)
        root.put("artboardHeight", doc.artboardHeight)
        root.put("targetExportWidth", doc.targetExportWidth)
        root.put("targetExportHeight", doc.targetExportHeight)
        if (doc.smartModeLabel != null) {
            root.put("smartModeLabel", doc.smartModeLabel)
        }
        root.put("dpi", doc.dpi)
        root.put("backgroundColor", doc.backgroundColor)
        if (doc.backgroundGradientEnd != null) {
            root.put("backgroundGradientEnd", doc.backgroundGradientEnd)
        }
        root.put("updatedAt", doc.updatedAt)

        val layersArr = JSONArray()
        doc.layers.forEach { layer ->
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

            when (layer) {
                is ShapeLayer -> {
                    obj.put("type", "shape")
                    obj.put("shapeType", layer.shapeType.name)
                    obj.put("fillColor", layer.fillColor)
                    if (layer.gradientEndColor != null) obj.put("gradientEndColor", layer.gradientEndColor)
                    obj.put("gradientAngle", layer.gradientAngle.toDouble())
                    obj.put("strokeColor", layer.strokeColor)
                    obj.put("strokeWidth", layer.strokeWidth.toDouble())
                    obj.put("cornerRadius", layer.cornerRadius.toDouble())
                    obj.put("polygonSides", layer.polygonSides)
                }
                is PathLayer -> {
                    obj.put("type", "path")
                    obj.put("strokeColor", layer.strokeColor)
                    obj.put("strokeWidth", layer.strokeWidth.toDouble())
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
                    if (layer.gradientEndColor != null) obj.put("gradientEndColor", layer.gradientEndColor)
                    obj.put("bold", layer.bold)
                    obj.put("italic", layer.italic)
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
        root.put("layers", layersArr)
        return root.toString()
    }

    fun fromJson(json: String): CanvasDocument {
        val root = JSONObject(json)
        val layersArr = root.optJSONArray("layers") ?: JSONArray()
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

            when (obj.optString("type")) {
                "shape" -> {
                    layers.add(
                        ShapeLayer(
                            id = id, name = name, visible = visible, locked = locked,
                            x = x, y = y, width = width, height = height,
                            rotation = rotation, opacity = opacity,
                            shapeType = ShapeType.entries.find { it.name == obj.optString("shapeType") } ?: ShapeType.RECTANGLE,
                            fillColor = obj.optLong("fillColor", 0xFFFF7A00L),
                            gradientEndColor = if (obj.has("gradientEndColor")) obj.optLong("gradientEndColor") else null,
                            gradientAngle = obj.optDouble("gradientAngle", 45.0).toFloat(),
                            strokeColor = obj.optLong("strokeColor", 0L),
                            strokeWidth = obj.optDouble("strokeWidth", 0.0).toFloat(),
                            cornerRadius = obj.optDouble("cornerRadius", 0.0).toFloat(),
                            polygonSides = obj.optInt("polygonSides", 6)
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
                            isPenSmooth = obj.optBoolean("isPenSmooth", true),
                            fillColor = if (obj.has("fillColor")) obj.optLong("fillColor") else null,
                            isClosed = obj.optBoolean("isClosed", false),
                            brushStyle = BrushStyle.entries.find { it.name == obj.optString("brushStyle") } ?: BrushStyle.ROUND
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
                            gradientEndColor = if (obj.has("gradientEndColor")) obj.optLong("gradientEndColor") else null,
                            bold = obj.optBoolean("bold", false),
                            italic = obj.optBoolean("italic", false),
                            align = TextAlignMode.entries.find { it.name == obj.optString("align") } ?: TextAlignMode.CENTER,
                            shadowEnabled = obj.optBoolean("shadowEnabled", false),
                            shadowColor = obj.optLong("shadowColor", 0x88000000L),
                            shadowRadius = obj.optDouble("shadowRadius", 8.0).toFloat(),
                            shadowOffsetX = obj.optDouble("shadowOffsetX", 4.0).toFloat(),
                            shadowOffsetY = obj.optDouble("shadowOffsetY", 4.0).toFloat(),
                            strokeColor = obj.optLong("strokeColor", 0xFFFFFFFFL),
                            strokeWidth = obj.optDouble("strokeWidth", 0.0).toFloat()
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
                            cornerRadiusRatio = obj.optDouble("cornerRadiusRatio", 0.0).toFloat()
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
                            secondaryColor = obj.optLong("secondaryColor", 0xFF1E1E1EL)
                        )
                    )
                }
            }
        }

        val artW = root.optInt("artboardWidth", 1080)
        val artH = root.optInt("artboardHeight", 1080)
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
            backgroundGradientEnd = if (root.has("backgroundGradientEnd")) root.optLong("backgroundGradientEnd") else null,
            layers = layers,
            updatedAt = root.optLong("updatedAt", System.currentTimeMillis())
        )
    }
}
