package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.DocumentSerializer
import com.example.model.CanvasDocument
import com.example.model.ExportFormat
import com.example.model.ExportResolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.min

data class ExportResult(
    val success: Boolean,
    val message: String,
    val shareUri: Uri? = null,
    val fileName: String = ""
)

object ExportManager {

    suspend fun exportToGalleryOrDownloads(
        context: Context,
        document: CanvasDocument,
        format: ExportFormat,
        resolution: ExportResolution,
        transparentBackground: Boolean,
        customWidthPx: Int? = null,
        customHeightPx: Int? = null,
        stretchToFit: Boolean = false
    ): ExportResult = withContext(Dispatchers.IO) {
        runCatching {
            val safeName = document.name.replace(Regex("[^a-zA-Z0-9_\\- ]"), "").trim().ifEmpty { "ArfinDesign" }
            val timestamp = System.currentTimeMillis()
            val fileName = "${safeName}_$timestamp.${format.ext}"

            val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cacheFile = File(cacheDir, fileName)

            val exportedDims = FileOutputStream(cacheFile).use { out ->
                writeDocumentBytes(
                    context = context,
                    document = document,
                    format = format,
                    resolution = resolution,
                    transparentBackground = transparentBackground,
                    customWidthPx = customWidthPx,
                    customHeightPx = customHeightPx,
                    stretchToFit = stretchToFit,
                    out = out
                )
            }

            // Save to Phone Gallery (Images) or Downloads (SVG/VECTOR/PDF) via MediaStore (zero broad permission on Android 10+)
            val isImage = format == ExportFormat.PNG || format == ExportFormat.JPG || format == ExportFormat.WEBP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collection = if (isImage) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
                val relativePath = if (isImage) {
                    "${Environment.DIRECTORY_PICTURES}/ArfinIllustrator"
                } else {
                    "${Environment.DIRECTORY_DOWNLOADS}/ArfinIllustrator"
                }
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val insertedUri = context.contentResolver.insert(collection, values)
                if (insertedUri != null) {
                    context.contentResolver.openOutputStream(insertedUri)?.use { os ->
                        cacheFile.inputStream().use { it.copyTo(os) }
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(insertedUri, values, null, null)
                }
            } else {
                val fallbackDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ArfinIllustrator").apply { mkdirs() }
                cacheFile.copyTo(File(fallbackDir, fileName), overwrite = true)
            }

            val shareUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val dimSuffix = if (exportedDims != null) " (${exportedDims.first}×${exportedDims.second}px)" else ""
            val locationMsg = if (isImage) "Saved to Gallery (Pictures/ArfinIllustrator)$dimSuffix" else "Saved to Downloads/ArfinIllustrator$dimSuffix"
            ExportResult(true, "$locationMsg • $fileName", shareUri, fileName)
        }.getOrElse { e ->
            ExportResult(false, e.message ?: "Export failed")
        }
    }

    suspend fun prepareShareUri(
        context: Context,
        document: CanvasDocument,
        format: ExportFormat = ExportFormat.PNG,
        resolution: ExportResolution = ExportResolution.RES_4K,
        transparentBackground: Boolean = false,
        customWidthPx: Int? = null,
        customHeightPx: Int? = null,
        stretchToFit: Boolean = false
    ): ExportResult = withContext(Dispatchers.IO) {
        runCatching {
            val safeName = document.name.replace(Regex("[^a-zA-Z0-9_\\- ]"), "").trim().ifEmpty { "ArfinDesign" }
            val fileName = "${safeName}_share.${format.ext}"
            val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cacheFile = File(cacheDir, fileName)
            FileOutputStream(cacheFile).use { out ->
                writeDocumentBytes(
                    context = context,
                    document = document,
                    format = format,
                    resolution = resolution,
                    transparentBackground = transparentBackground,
                    customWidthPx = customWidthPx,
                    customHeightPx = customHeightPx,
                    stretchToFit = stretchToFit,
                    out = out
                )
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
            ExportResult(true, "Ready to share", uri, fileName)
        }.getOrElse { e ->
            ExportResult(false, e.message ?: "Share preparation failed")
        }
    }

    fun launchShareIntent(context: Context, uri: Uri, mimeType: String, targetPackage: String? = null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (targetPackage != null) {
                setPackage(targetPackage)
            }
        }
        try {
            if (targetPackage != null) {
                context.startActivity(intent)
            } else {
                context.startActivity(Intent.createChooser(intent, "Share Design via Arfin Illustrator"))
            }
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallback, "Share Design"))
        }
    }

    private fun renderTargetBitmap(
        context: Context,
        document: CanvasDocument,
        resolution: ExportResolution,
        transparentBackground: Boolean,
        customWidthPx: Int?,
        customHeightPx: Int?,
        stretchToFit: Boolean
    ): Bitmap {
        return if (customWidthPx != null && customHeightPx != null && customWidthPx > 0 && customHeightPx > 0) {
            CanvasRenderer.renderCustomResolutionBitmap(
                context = context,
                doc = document,
                customWidth = customWidthPx,
                customHeight = customHeightPx,
                transparentBackground = transparentBackground,
                stretchToFit = stretchToFit
            )
        } else {
            CanvasRenderer.renderExportBitmap(context, document, resolution, transparentBackground)
        }
    }

    private fun writeDocumentBytes(
        context: Context,
        document: CanvasDocument,
        format: ExportFormat,
        resolution: ExportResolution,
        transparentBackground: Boolean,
        customWidthPx: Int?,
        customHeightPx: Int?,
        stretchToFit: Boolean,
        out: OutputStream
    ): Pair<Int, Int>? {
        return when (format) {
            ExportFormat.PNG -> {
                val bmp = renderTargetBitmap(context, document, resolution, transparentBackground, customWidthPx, customHeightPx, stretchToFit)
                val dims = bmp.width to bmp.height
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                bmp.recycle()
                dims
            }
            ExportFormat.JPG -> {
                val bmp = renderTargetBitmap(context, document, resolution, false, customWidthPx, customHeightPx, stretchToFit)
                val dims = bmp.width to bmp.height
                bmp.compress(Bitmap.CompressFormat.JPEG, 96, out)
                bmp.recycle()
                dims
            }
            ExportFormat.WEBP -> {
                val bmp = renderTargetBitmap(context, document, resolution, transparentBackground, customWidthPx, customHeightPx, stretchToFit)
                val dims = bmp.width to bmp.height
                val compressFmt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                bmp.compress(compressFmt, 95, out)
                bmp.recycle()
                dims
            }
            ExportFormat.SVG -> {
                val svg = SvgExporter.toSvg(document, transparentBackground)
                out.write(svg.toByteArray(Charsets.UTF_8))
                null
            }
            ExportFormat.VECTOR -> {
                val json = DocumentSerializer.toJson(document)
                out.write(json.toByteArray(Charsets.UTF_8))
                null
            }
            ExportFormat.PDF -> {
                val pageW = (customWidthPx ?: document.targetExportWidth).coerceIn(50, 20000)
                val pageH = (customHeightPx ?: document.targetExportHeight).coerceIn(50, 20000)
                val pdf = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
                val page = pdf.startPage(pageInfo)
                if (stretchToFit) {
                    val sx = pageW.toFloat() / document.artboardWidth.toFloat()
                    val sy = pageH.toFloat() / document.artboardHeight.toFloat()
                    CanvasRenderer.renderDocument(context, page.canvas, document, sx, sy, transparentBackground = false)
                } else {
                    val fit = min(
                        pageW.toFloat() / document.artboardWidth.toFloat(),
                        pageH.toFloat() / document.artboardHeight.toFloat()
                    )
                    val ox = (pageW - document.artboardWidth * fit) / 2f
                    val oy = (pageH - document.artboardHeight * fit) / 2f
                    page.canvas.save()
                    page.canvas.translate(ox, oy)
                    CanvasRenderer.renderDocument(context, page.canvas, document, fit, fit, transparentBackground = false)
                    page.canvas.restore()
                }
                pdf.finishPage(page)
                pdf.writeTo(out)
                pdf.close()
                pageW to pageH
            }
        }
    }
}
