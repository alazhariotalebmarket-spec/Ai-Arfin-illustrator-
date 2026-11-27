package com.example

import com.example.model.*
import com.example.ui.theme.FontCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun catalogs_and_charts_meet_requirements() {
        assertTrue(FontCatalog.fonts.size >= 50)
        assertTrue(StickerCatalog.items.size >= 115)
        assertEquals("Bangladesh Crest", StickerCatalog.findById("bd_flag").title)
        assertEquals(20, TemplateCatalog.templates.size)
        assertEquals(20, CustomSizeChartCatalog.inchChart.size)
        assertEquals(20, CustomSizeChartCatalog.feetChart.size)
        assertEquals(300, CustomSizeChartCatalog.inchChart.first().targetWidthPx)
        assertEquals(6000, CustomSizeChartCatalog.inchChart.last().targetHeightPx)
        assertEquals(3600, CustomSizeChartCatalog.feetChart.first().targetWidthPx)
        assertEquals(72000, CustomSizeChartCatalog.feetChart.last().targetHeightPx)
    }

    @Test
    fun smart_mode_scales_huge_feet_banner_safely() {
        val doc = CanvasDocument.createSmart(
            name = "1x20ft",
            targetWidthPx = 3600,
            targetHeightPx = 72000,
            dpi = 300
        )
        assertTrue(doc.artboardHeight <= 1440)
        assertEquals(3600, doc.targetExportWidth)
        assertEquals(72000, doc.targetExportHeight)
        assertTrue(doc.smartModeLabel != null)
    }

    @Test
    fun all_20_templates_build_valid_documents() {
        TemplateCatalog.templates.forEach { tpl ->
            val doc = tpl.documentBuilder()
            assertTrue(doc.artboardWidth > 0)
            assertTrue(doc.artboardHeight > 0)
            assertTrue(doc.layers.isNotEmpty())
        }
    }

    @Test
    fun custom_resolution_and_conversion_math_work_cleanly() {
        val (safeW, safeH) = com.example.engine.CanvasRenderer.resolveSafeExportDimensions(2400, 1350)
        assertEquals(2400, safeW)
        assertEquals(1350, safeH)

        val (cappedW, cappedH) = com.example.engine.CanvasRenderer.resolveSafeExportDimensions(16000, 16000)
        assertTrue(cappedW.toLong() * cappedH.toLong() <= 8192L * 4608L + 1000L)

        assertEquals(3600, CustomSizeChartCatalog.convertToPixels(12f, SizeUnit.INCH, 300))
        assertEquals(1500, CustomSizeChartCatalog.convertToPixels(5f, SizeUnit.INCH, 300))

        val img = ImageLayer(
            name = "Cutout",
            imagePath = "/tmp/cut.png",
            originalImagePath = "/tmp/orig.png",
            cornerRadiusRatio = 0.5f
        )
        assertEquals("/tmp/orig.png", img.originalImagePath)
        assertEquals(0.5f, img.cornerRadiusRatio, 0.001f)
    }
}
