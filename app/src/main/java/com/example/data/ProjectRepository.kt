package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.engine.CanvasRenderer
import com.example.model.CanvasDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ProjectRepository(
    private val context: Context,
    private val projectDao: ProjectDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.observeAllProjects()

    private val projectsDir: File by lazy {
        File(context.filesDir, "projects").apply { mkdirs() }
    }

    suspend fun saveProject(document: CanvasDocument, existingCreatedAt: Long? = null): ProjectEntity =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val updatedDoc = document.copy(updatedAt = now)
            val json = DocumentSerializer.toJson(updatedDoc)
            val thumbFile = File(projectsDir, "thumb_${updatedDoc.id}.png")

            // Render crisp 360px max-edge thumbnail offline
            val thumbBitmap = CanvasRenderer.renderThumbnail(context, updatedDoc, maxDimension = 360)
            FileOutputStream(thumbFile).use { out ->
                thumbBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            thumbBitmap.recycle()

            val existing = if (existingCreatedAt == null) projectDao.getProjectById(updatedDoc.id) else null
            val entity = ProjectEntity(
                id = updatedDoc.id,
                name = updatedDoc.name,
                category = updatedDoc.category,
                artboardWidth = updatedDoc.artboardWidth,
                artboardHeight = updatedDoc.artboardHeight,
                documentJson = json,
                thumbnailPath = thumbFile.absolutePath,
                createdAt = existingCreatedAt ?: existing?.createdAt ?: now,
                updatedAt = now
            )
            projectDao.upsertProject(entity)
            entity
        }

    suspend fun loadDocument(projectId: String): CanvasDocument? = withContext(Dispatchers.IO) {
        val entity = projectDao.getProjectById(projectId) ?: return@withContext null
        DocumentSerializer.fromJson(entity.documentJson)
    }

    suspend fun duplicateProject(project: ProjectEntity): ProjectEntity = withContext(Dispatchers.IO) {
        val original = DocumentSerializer.fromJson(project.documentJson)
        val cloned = original.copy(
            id = UUID.randomUUID().toString(),
            name = "${original.name} (Copy)",
            updatedAt = System.currentTimeMillis()
        )
        saveProject(cloned)
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(projectId)
        val thumbFile = File(projectsDir, "thumb_$projectId.png")
        if (thumbFile.exists()) thumbFile.delete()
    }

    data class ImportedImageResult(
        val path: String,
        val width: Int,
        val height: Int
    )

    suspend fun importImageHighRes(uri: Uri, maxEdge: Int = 4096): ImportedImageResult? = withContext(Dispatchers.IO) {
        runCatching {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }

            var inSample = 1
            val longestRaw = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
            if (longestRaw > maxEdge) {
                while (longestRaw / (inSample * 2) >= maxEdge) {
                    inSample *= 2
                }
            }

            val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                runCatching {
                    android.media.ExifInterface(stream).getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                }.getOrDefault(android.media.ExifInterface.ORIENTATION_NORMAL)
            } ?: android.media.ExifInterface.ORIENTATION_NORMAL

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = inSample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return@withContext null

            val rotated = applyExifOrientation(decoded, orientation)
            if (rotated !== decoded) decoded.recycle()

            val scaled = downscaleIfNeeded(rotated, maxEdge = maxEdge)
            if (scaled !== rotated) rotated.recycle()

            val file = File(projectsDir, "img_${UUID.randomUUID()}.png")
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val finalW = scaled.width
            val finalH = scaled.height
            scaled.recycle()
            ImportedImageResult(file.absolutePath, finalW, finalH)
        }.getOrNull()
    }

    suspend fun importImageToLocalStorage(uri: Uri): String? =
        importImageHighRes(uri)?.path

    private fun applyExifOrientation(src: Bitmap, orientation: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        when (orientation) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            android.media.ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            android.media.ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return src
        }
        return runCatching {
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        }.getOrDefault(src)
    }

    suspend fun saveBitmapToLocalStorage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val file = File(projectsDir, "img_${UUID.randomUUID()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file.absolutePath
    }

    suspend fun importVectorBackup(uri: Uri): CanvasDocument? = withContext(Dispatchers.IO) {
        runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext null
            val parsed = DocumentSerializer.fromJson(raw)
            val imported = parsed.copy(
                id = UUID.randomUUID().toString(),
                updatedAt = System.currentTimeMillis()
            )
            saveProject(imported)
            imported
        }.getOrNull()
    }

    private fun downscaleIfNeeded(src: Bitmap, maxEdge: Int): Bitmap {
        val w = src.width
        val h = src.height
        val longest = maxOf(w, h)
        if (longest <= maxEdge) return src
        val ratio = maxEdge.toFloat() / longest.toFloat()
        val nw = (w * ratio).toInt().coerceAtLeast(1)
        val nh = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, nw, nh, true)
    }
}
