package com.example.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object BackgroundRemover {

    /**
     * Offline smart background remover:
     * Samples border corners and runs a fast flood-fill + soft alpha feathering
     * so solid/studio backgrounds become cleanly transparent without internet.
     */
    suspend fun removeBackground(
        imagePath: String,
        tolerance: Float = 0.22f, // 0.05..0.6
        globalKeyMode: Boolean = false
    ): Bitmap? = withContext(Dispatchers.Default) {
        val src = BitmapFactory.decodeFile(imagePath) ?: return@withContext null
        val w = src.width
        val h = src.height
        if (w == 0 || h == 0) return@withContext null

        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        src.recycle()

        // Sample average color of the 4 corners
        val cornerSamples = listOf(
            pixels[0],
            pixels[w - 1],
            pixels[(h - 1) * w],
            pixels[h * w - 1]
        )
        val refR = cornerSamples.map { Color.red(it) }.average().toInt()
        val refG = cornerSamples.map { Color.green(it) }.average().toInt()
        val refB = cornerSamples.map { Color.blue(it) }.average().toInt()

        val maxDist = 442f * tolerance.coerceIn(0.05f, 0.7f)
        val featherRange = max(12f, maxDist * 0.25f)

        fun colorDist(pixel: Int): Float {
            val dr = Color.red(pixel) - refR
            val dg = Color.green(pixel) - refG
            val db = Color.blue(pixel) - refB
            return sqrt((dr * dr + dg * dg + db * db).toFloat())
        }

        if (globalKeyMode) {
            for (i in pixels.indices) {
                val p = pixels[i]
                val d = colorDist(p)
                if (d < maxDist) {
                    pixels[i] = Color.argb(0, Color.red(p), Color.green(p), Color.blue(p))
                } else if (d < maxDist + featherRange) {
                    val ratio = ((d - maxDist) / featherRange).coerceIn(0f, 1f)
                    val newA = (Color.alpha(p) * ratio).toInt()
                    pixels[i] = Color.argb(newA, Color.red(p), Color.green(p), Color.blue(p))
                }
            }
        } else {
            val visited = BooleanArray(w * h)
            val queue = IntArray(w * h)
            var head = 0
            var tail = 0

            fun tryEnqueue(idx: Int) {
                if (!visited[idx] && colorDist(pixels[idx]) <= maxDist + featherRange) {
                    visited[idx] = true
                    queue[tail++] = idx
                }
            }

            // Seed from all 4 image edges
            for (x in 0 until w) {
                tryEnqueue(x)
                tryEnqueue((h - 1) * w + x)
            }
            for (y in 0 until h) {
                tryEnqueue(y * w)
                tryEnqueue(y * w + (w - 1))
            }

            while (head < tail) {
                val idx = queue[head++]
                val p = pixels[idx]
                val d = colorDist(p)
                if (d <= maxDist) {
                    pixels[idx] = Color.argb(0, Color.red(p), Color.green(p), Color.blue(p))
                } else {
                    val ratio = ((d - maxDist) / featherRange).coerceIn(0f, 1f)
                    val newA = (Color.alpha(p) * ratio).toInt()
                    pixels[idx] = Color.argb(newA, Color.red(p), Color.green(p), Color.blue(p))
                    continue // don't expand past feathered boundary
                }

                val x = idx % w
                val y = idx / w
                if (x > 0) tryEnqueue(idx - 1)
                if (x < w - 1) tryEnqueue(idx + 1)
                if (y > 0) tryEnqueue(idx - w)
                if (y < h - 1) tryEnqueue(idx + w)
            }
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        out
    }
}
