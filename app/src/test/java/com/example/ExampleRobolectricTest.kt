package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DocumentSerializer
import com.example.engine.CanvasRenderer
import com.example.engine.SvgExporter
import com.example.model.StickerCatalog
import com.example.model.TemplateCatalog
import com.example.ui.theme.FontCatalog
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Arfin Illustrator`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Arfin Illustrator", appName)
    }

    @Test
    fun `catalogs contain 50+ fonts and 100+ stickers`() {
        assertTrue(FontCatalog.fonts.size >= 50)
        assertTrue(StickerCatalog.items.size >= 100)
        assertTrue(TemplateCatalog.templates.size >= 5)
    }

    @Test
    fun `template document serializes, exports svg, and renders thumbnail`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val template = TemplateCatalog.templates.first()
        val doc = template.documentBuilder()

        val json = DocumentSerializer.toJson(doc)
        val restored = DocumentSerializer.fromJson(json)
        assertEquals(doc.name, restored.name)
        assertEquals(doc.layers.size, restored.layers.size)

        val svg = SvgExporter.toSvg(restored, transparentBackground = false)
        assertTrue(svg.contains("<svg"))
        assertTrue(svg.contains("</svg>"))

        val thumb = CanvasRenderer.renderThumbnail(context, restored, maxDimension = 200)
        assertTrue(thumb.width > 0 && thumb.height > 0)
        thumb.recycle()
    }

    @Test
    fun `super resolution engine upscales and restores pixels cleanly`() = kotlinx.coroutines.runBlocking {
        val lowRes = android.graphics.Bitmap.createBitmap(64, 48, android.graphics.Bitmap.Config.ARGB_8888)
        lowRes.eraseColor(android.graphics.Color.rgb(40, 120, 220))
        // Draw crisp inner stripe to exercise edge-directed enhancement
        for (y in 12 until 36) {
            for (x in 16 until 48) {
                lowRes.setPixel(x, y, android.graphics.Color.rgb(255, 122, 0))
            }
        }

        val enhanced = com.example.engine.SuperResolutionEngine.enhanceBitmap(
            src = lowRes,
            targetMaxEdge = 1080,
            profile = com.example.engine.PixelRepairProfile.BALANCED_HD
        )
        assertTrue(enhanced.width >= 192)
        assertTrue(enhanced.height >= 144)
        assertEquals(255, android.graphics.Color.alpha(enhanced.getPixel(enhanced.width / 2, enhanced.height / 2)))
        lowRes.recycle()
        enhanced.recycle()
    }
}
