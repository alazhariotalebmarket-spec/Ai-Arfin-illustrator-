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

    suspend fun importImageToLocalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val decoded = input.use { BitmapFactory.decodeStream(it) } ?: return@withContext null
            val scaled = downscaleIfNeeded(decoded, maxEdge = 2048)
            val file = File(projectsDir, "img_${UUID.randomUUID()}.png")
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            if (scaled !== decoded) decoded.recycle()
            scaled.recycle()
            file.absolutePath
        }.getOrNull()
    }

    suspend fun saveBitmapToLocalStorage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val file = File(projectsDir, "img_${UUID.randomUUID()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
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
