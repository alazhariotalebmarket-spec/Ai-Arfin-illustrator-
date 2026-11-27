package com.example.model

import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

enum class ArtboardPreset(
    val id: String,
    val label: String,
    val labelBn: String,
    val width: Int,
    val height: Int,
    val category: String
) {
    LOGO("logo", "Logo", "লোগো", 1000, 1000, "Logo"),
    FACEBOOK_POST("fb_post", "Facebook Post", "সোশ্যাল পোস্ট", 1080, 1080, "Social Post"),
    YOUTUBE_THUMB("yt_thumb", "YouTube Thumbnail", "ইউটিউব থাম্বনেইল", 1280, 720, "YouTube Thumbnail"),
    STORY("story", "Story / Reel", "স্টোরি", 1080, 1920, "Poster"),
    BANNER("banner", "Banner", "ব্যানার", 1200, 628, "Banner"),
    A4("a4", "A4 Poster", "এ৪ পোস্টার", 1240, 1754, "Poster"),
    BUSINESS_CARD("biz_card", "Business Card", "ভিজিটিং কার্ড", 1050, 600, "Business Card"),
    ISLAMIC("islamic", "Islamic Design", "ইসলামিক ডিজাইন", 1080, 1080, "Islamic Design"),
    CUSTOM("custom", "Custom Size", "কাস্টম সাইজ", 1080, 1080, "Blank Canvas")
}

enum class SizeUnit(val label: String) {
    INCH("Inch"),
    FEET("Feet"),
    CM("Cm"),
    PIXEL("Pixel")
}

data class SizeChartEntry(
    val physicalLabel: String,
    val pixelLabel: String,
    val targetWidthPx: Int,
    val targetHeightPx: Int,
    val isFeet: Boolean
)

object CustomSizeChartCatalog {
    val inchChart: List<SizeChartEntry> = (1..20).map { h ->
        SizeChartEntry(
            physicalLabel = "1 inch × $h inch",
            pixelLabel = "300 × ${h * 300} px",
            targetWidthPx = 300,
            targetHeightPx = h * 300,
            isFeet = false
        )
    }

    val feetChart: List<SizeChartEntry> = (1..20).map { h ->
        SizeChartEntry(
            physicalLabel = "1 ft × $h ft",
            pixelLabel = "3600 × ${h * 3600} px",
            targetWidthPx = 3600,
            targetHeightPx = h * 3600,
            isFeet = true
        )
    }

    fun convertToPixels(value: Float, unit: SizeUnit, dpi: Int): Int {
        val px = when (unit) {
            SizeUnit.INCH -> value * dpi
            SizeUnit.FEET -> value * 12f * dpi
            SizeUnit.CM -> (value / 2.54f) * dpi
            SizeUnit.PIXEL -> value
        }
        return px.roundToInt().coerceIn(50, 100_000)
    }
}

enum class ToolMode(val label: String, val labelBn: String) {
    SELECT("Select", "সিলেক্ট"),
    PEN("Pen", "পেন টুল"),
    BRUSH("Brush", "ব্রাশ"),
    SHAPES("Shapes", "শেপ"),
    TEXT("Text", "টেক্সট"),
    IMAGE("Image", "ছবি"),
    STICKER("Stickers", "স্টিকার"),
    COLOR("Color", "কালার"),
    ERASER("Eraser", "ইরেজার"),
    ALIGN("Align", "এলাইন"),
    EYEDROPPER("Picker", "আইড্রপার"),
    ZOOM("Zoom", "জুম")
}

enum class ShapeType(val label: String) {
    RECTANGLE("Rectangle"),
    CIRCLE("Circle"),
    TRIANGLE("Triangle"),
    LINE("Line"),
    STAR("Star"),
    POLYGON("Polygon")
}

enum class BrushStyle(val label: String) {
    ROUND("Smooth Round"),
    CALLIGRAPHY("Calligraphy Nib"),
    MARKER("Bold Marker")
}

enum class TextAlignMode(val label: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right")
}

enum class AlignAction(val label: String) {
    LEFT("Align Left"),
    CENTER_H("Center H"),
    RIGHT("Align Right"),
    TOP("Align Top"),
    CENTER_V("Center V"),
    BOTTOM("Align Bottom")
}

enum class ExportFormat(val ext: String, val label: String, val mimeType: String, val supportsTransparency: Boolean) {
    PNG("png", "PNG", "image/png", true),
    JPG("jpg", "JPG", "image/jpeg", false),
    WEBP("webp", "WEBP", "image/webp", true),
    SVG("svg", "SVG", "image/svg+xml", true),
    VECTOR("aivec.json", "VECTOR", "application/json", true),
    PDF("pdf", "PDF", "application/pdf", false)
}

enum class ExportResolution(val label: String, val subtitle: String, val multiplierOrTargetMax: Int, val isFixedMaxEdge: Boolean) {
    RES_1X("1x", "Original / 300 DPI Smart Target", 1, false),
    RES_2X("2x", "2x Crisp HD", 2, false),
    RES_4K("4K", "4K High Resolution (Default)", 3840, true),
    RES_8K("8K", "8K Ultra HD", 7680, true)
}

data class PathPoint(
    val x: Float,
    val y: Float
)

sealed interface LayerElement {
    val id: String
    val name: String
    val visible: Boolean
    val locked: Boolean
    val x: Float
    val y: Float
    val width: Float
    val height: Float
    val rotation: Float
    val opacity: Float

    fun withBase(
        name: String = this.name,
        visible: Boolean = this.visible,
        locked: Boolean = this.locked,
        x: Float = this.x,
        y: Float = this.y,
        width: Float = this.width,
        height: Float = this.height,
        rotation: Float = this.rotation,
        opacity: Float = this.opacity
    ): LayerElement

    fun duplicateWithNewId(): LayerElement
}

data class ShapeLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Shape",
    override val visible: Boolean = true,
    override val locked: Boolean = false,
    override val x: Float = 100f,
    override val y: Float = 100f,
    override val width: Float = 300f,
    override val height: Float = 300f,
    override val rotation: Float = 0f,
    override val opacity: Float = 1f,
    val shapeType: ShapeType = ShapeType.RECTANGLE,
    val fillColor: Long = 0xFFFF7A00L,
    val gradientEndColor: Long? = null,
    val gradientAngle: Float = 45f,
    val strokeColor: Long = 0x00000000L,
    val strokeWidth: Float = 0f,
    val cornerRadius: Float = 0f,
    val polygonSides: Int = 6
) : LayerElement {
    override fun withBase(
        name: String, visible: Boolean, locked: Boolean,
        x: Float, y: Float, width: Float, height: Float, rotation: Float, opacity: Float
    ): LayerElement = copy(
        name = name, visible = visible, locked = locked,
        x = x, y = y, width = width, height = height, rotation = rotation, opacity = opacity
    )

    override fun duplicateWithNewId(): LayerElement =
        copy(id = UUID.randomUUID().toString(), name = "$name Copy", x = x + 30f, y = y + 30f)
}

data class PathLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Vector Path",
    override val visible: Boolean = true,
    override val locked: Boolean = false,
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val width: Float = 100f,
    override val height: Float = 100f,
    override val rotation: Float = 0f,
    override val opacity: Float = 1f,
    val points: List<PathPoint> = emptyList(),
    val strokeColor: Long = 0xFF1E1E1EL,
    val strokeWidth: Float = 10f,
    val isPenSmooth: Boolean = true,
    val fillColor: Long? = null,
    val isClosed: Boolean = false,
    val brushStyle: BrushStyle = BrushStyle.ROUND
) : LayerElement {
    override fun withBase(
        name: String, visible: Boolean, locked: Boolean,
        x: Float, y: Float, width: Float, height: Float, rotation: Float, opacity: Float
    ): LayerElement = copy(
        name = name, visible = visible, locked = locked,
        x = x, y = y, width = width, height = height, rotation = rotation, opacity = opacity
    )

    override fun duplicateWithNewId(): LayerElement =
        copy(id = UUID.randomUUID().toString(), name = "$name Copy", x = x + 24f, y = y + 24f)
}

data class TextLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Text",
    override val visible: Boolean = true,
    override val locked: Boolean = false,
    override val x: Float = 150f,
    override val y: Float = 300f,
    override val width: Float = 600f,
    override val height: Float = 160f,
    override val rotation: Float = 0f,
    override val opacity: Float = 1f,
    val text: String = "নতুন টেক্সট • Text",
    val fontId: String = "hind_siliguri",
    val fontSize: Float = 72f,
    val color: Long = 0xFF1E1E1EL,
    val gradientEndColor: Long? = null,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val align: TextAlignMode = TextAlignMode.CENTER,
    val shadowEnabled: Boolean = false,
    val shadowColor: Long = 0x88000000L,
    val shadowRadius: Float = 8f,
    val shadowOffsetX: Float = 4f,
    val shadowOffsetY: Float = 4f,
    val strokeColor: Long = 0xFFFFFFFFL,
    val strokeWidth: Float = 0f
) : LayerElement {
    override fun withBase(
        name: String, visible: Boolean, locked: Boolean,
        x: Float, y: Float, width: Float, height: Float, rotation: Float, opacity: Float
    ): LayerElement = copy(
        name = name, visible = visible, locked = locked,
        x = x, y = y, width = width, height = height, rotation = rotation, opacity = opacity
    )

    override fun duplicateWithNewId(): LayerElement =
        copy(id = UUID.randomUUID().toString(), name = "$name Copy", x = x + 28f, y = y + 28f)
}

data class ImageLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Image",
    override val visible: Boolean = true,
    override val locked: Boolean = false,
    override val x: Float = 140f,
    override val y: Float = 140f,
    override val width: Float = 500f,
    override val height: Float = 500f,
    override val rotation: Float = 0f,
    override val opacity: Float = 1f,
    val imagePath: String = "",
    val originalImagePath: String = imagePath,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
    val cornerRadiusRatio: Float = 0f
) : LayerElement {
    override fun withBase(
        name: String, visible: Boolean, locked: Boolean,
        x: Float, y: Float, width: Float, height: Float, rotation: Float, opacity: Float
    ): LayerElement = copy(
        name = name, visible = visible, locked = locked,
        x = x, y = y, width = width, height = height, rotation = rotation, opacity = opacity
    )

    override fun duplicateWithNewId(): LayerElement =
        copy(id = UUID.randomUUID().toString(), name = "$name Copy", x = x + 30f, y = y + 30f)
}

data class StickerLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Sticker",
    override val visible: Boolean = true,
    override val locked: Boolean = false,
    override val x: Float = 200f,
    override val y: Float = 200f,
    override val width: Float = 260f,
    override val height: Float = 260f,
    override val rotation: Float = 0f,
    override val opacity: Float = 1f,
    val stickerId: String = "isl_crescent",
    val primaryColor: Long = 0xFFFF7A00L,
    val secondaryColor: Long = 0xFF1E1E1EL
) : LayerElement {
    override fun withBase(
        name: String, visible: Boolean, locked: Boolean,
        x: Float, y: Float, width: Float, height: Float, rotation: Float, opacity: Float
    ): LayerElement = copy(
        name = name, visible = visible, locked = locked,
        x = x, y = y, width = width, height = height, rotation = rotation, opacity = opacity
    )

    override fun duplicateWithNewId(): LayerElement =
        copy(id = UUID.randomUUID().toString(), name = "$name Copy", x = x + 30f, y = y + 30f)
}

data class CanvasDocument(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled Design",
    val category: String = "Blank Canvas",
    val artboardWidth: Int = 1080,
    val artboardHeight: Int = 1080,
    val targetExportWidth: Int = artboardWidth,
    val targetExportHeight: Int = artboardHeight,
    val smartModeLabel: String? = null,
    val dpi: Int = 300,
    val backgroundColor: Long = 0xFFFFFFFFL,
    val backgroundGradientEnd: Long? = null,
    val layers: List<LayerElement> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Smart Mode: Keeps interactive canvas responsive (max edge <= 1440px, min edge >= 120px)
         * while preserving exact aspect ratio and storing true 300 DPI target dimensions for export.
         */
        fun createSmart(
            name: String,
            targetWidthPx: Int,
            targetHeightPx: Int,
            dpi: Int = 300,
            category: String = "Banner",
            backgroundColor: Long = 0xFFFFFFFFL,
            smartLabel: String? = null
        ): CanvasDocument {
            val safeTargetW = targetWidthPx.coerceIn(50, 100_000)
            val safeTargetH = targetHeightPx.coerceIn(50, 100_000)
            val longest = max(safeTargetW, safeTargetH)
            val isSmart = longest > 1800
            val (workspaceW, workspaceH) = if (isSmart) {
                val scale = 1440f / longest.toFloat()
                max(120, (safeTargetW * scale).roundToInt()) to max(120, (safeTargetH * scale).roundToInt())
            } else {
                safeTargetW to safeTargetH
            }
            val label = smartLabel ?: if (isSmart) "Smart Mode • $dpi DPI (${safeTargetW}×${safeTargetH}px)" else null
            return CanvasDocument(
                name = name,
                category = category,
                artboardWidth = workspaceW,
                artboardHeight = workspaceH,
                targetExportWidth = safeTargetW,
                targetExportHeight = safeTargetH,
                smartModeLabel = label,
                dpi = dpi,
                backgroundColor = backgroundColor
            )
        }
    }
}
