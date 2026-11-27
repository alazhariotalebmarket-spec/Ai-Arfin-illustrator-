package com.example.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object BackgroundRemover {

    /**
     * Samples the RGB color at a normalized point (0f..1f, 0f..1f) on the image.
     */
    suspend fun sampleColorAtNormalizedPoint(
        imagePath: String,
        normX: Float,
        normY: Float
    ): Int? = withContext(Dispatchers.IO) {
        runCatching {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(imagePath, opts)
            val sampleSize = max(1, max(opts.outWidth, opts.outHeight) / 1024)
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bmp = BitmapFactory.decodeFile(imagePath, decodeOpts) ?: return@withContext null
            val px = (normX.coerceIn(0f, 1f) * (bmp.width - 1)).roundToInt().coerceIn(0, bmp.width - 1)
            val py = (normY.coerceIn(0f, 1f) * (bmp.height - 1)).roundToInt().coerceIn(0, bmp.height - 1)
            val color = bmp.getPixel(px, py)
            bmp.recycle()
            color
        }.getOrNull()
    }

    /**
     * Offline smart background remover:
     * Uses either an explicit user-picked background color or automatic border sampling,
     * runs a fast flood-fill (or global color key) with smooth edge feathering for crisp 4K/8K PNGs.
     */
    suspend fun removeBackground(
        imagePath: String,
        tolerance: Float = 0.22f, // 0.05..0.65
        globalKeyMode: Boolean = false,
        targetColor: Int? = null,
        edgeSmoothness: Float = 0.28f
    ): Bitmap? = withContext(Dispatchers.Default) {
        val src = BitmapFactory.decodeFile(imagePath) ?: return@withContext null
        val w = src.width
        val h = src.height
        if (w == 0 || h == 0) return@withContext null

        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        src.recycle()

        val (refR, refG, refB) = if (targetColor != null) {
            Triple(Color.red(targetColor), Color.green(targetColor), Color.blue(targetColor))
        } else {
            // Sample corners + edge midpoints for robust automatic backdrop detection
            val edgeSamples = listOf(
                pixels[0],
                pixels[w - 1],
                pixels[(h - 1) * w],
                pixels[h * w - 1],
                pixels[w / 2],
                pixels[(h - 1) * w + w / 2],
                pixels[(h / 2) * w],
                pixels[(h / 2) * w + (w - 1)]
            ).filter { Color.alpha(it) > 200 }

            val validSamples = if (edgeSamples.isNotEmpty()) edgeSamples else listOf(pixels[0])
            Triple(
                validSamples.map { Color.red(it) }.sorted()[validSamples.size / 2],
                validSamples.map { Color.green(it) }.sorted()[validSamples.size / 2],
                validSamples.map { Color.blue(it) }.sorted()[validSamples.size / 2]
            )
        }

        val maxDist = 442f * tolerance.coerceIn(0.05f, 0.72f)
        val featherRange = max(10f, maxDist * edgeSmoothness.coerceIn(0.10f, 0.65f))

        fun colorDist(pixel: Int): Float {
            val dr = Color.red(pixel) - refR
            val dg = Color.green(pixel) - refG
            val db = Color.blue(pixel) - refB
            return sqrt((dr * dr + dg * dg + db * db).toFloat())
        }

        if (globalKeyMode) {
            for (i in pixels.indices) {
                val p = pixels[i]
                val a = Color.alpha(p)
                if (a == 0) continue
                val d = colorDist(p)
                if (d < maxDist) {
                    pixels[i] = Color.argb(0, Color.red(p), Color.green(p), Color.blue(p))
                } else if (d < maxDist + featherRange) {
                    val ratio = ((d - maxDist) / featherRange).coerceIn(0f, 1f)
                    val newA = (a * ratio).toInt()
                    pixels[i] = Color.argb(newA, Color.red(p), Color.green(p), Color.blue(p))
                }
            }
        } else {
            val visited = BooleanArray(w * h)
            val queue = IntArray(w * h)
            var head = 0
            var tail = 0

            fun tryEnqueue(idx: Int) {
                if (visited[idx]) return
                val p = pixels[idx]
                if (Color.alpha(p) == 0 || colorDist(p) <= maxDist + featherRange) {
                    visited[idx] = true
                    queue[tail++] = idx
                }
            }

            // Seed from all 4 image edges (or also matching pixels if targetColor was explicitly picked)
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
                val a = Color.alpha(p)
                if (a == 0) {
                    // Already transparent: keep propagating across transparent border
                } else {
                    val d = colorDist(p)
                    if (d <= maxDist) {
                        pixels[idx] = Color.argb(0, Color.red(p), Color.green(p), Color.blue(p))
                    } else {
                        val ratio = ((d - maxDist) / featherRange).coerceIn(0f, 1f)
                        val newA = (a * ratio).toInt()
                        pixels[idx] = Color.argb(newA, Color.red(p), Color.green(p), Color.blue(p))
                        continue
                    }
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

    /**
     * Extracts the selected portion of a photo at full source resolution,
     * optionally upscales small cropped portions up to HD/4K (progressive bicubic)
     * and applies a crisp unsharp-mask clarity filter while keeping alpha transparent.
     */
    suspend fun cropAndEnhanceToHighRes(
        imagePath: String,
        cropLeft: Float,
        cropTop: Float,
        cropRight: Float,
        cropBottom: Float,
        upscaleToHighRes: Boolean = true,
        targetMaxEdge: Int = 3072,
        sharpenAmount: Float = 0.45f
    ): Bitmap? = withContext(Dispatchers.Default) {
        val src = BitmapFactory.decodeFile(imagePath) ?: return@withContext null
        val sw = src.width
        val sh = src.height
        if (sw <= 0 || sh <= 0) return@withContext null

        val l = cropLeft.coerceIn(0f, 0.95f)
        val t = cropTop.coerceIn(0f, 0.95f)
        val r = cropRight.coerceIn(l + 0.04f, 1f)
        val b = cropBottom.coerceIn(t + 0.04f, 1f)

        val x = (l * sw).roundToInt().coerceIn(0, sw - 2)
        val y = (t * sh).roundToInt().coerceIn(0, sh - 2)
        val cw = ((r - l) * sw).roundToInt().coerceIn(2, sw - x)
        val ch = ((b - t) * sh).roundToInt().coerceIn(2, sh - y)

        var working = Bitmap.createBitmap(src, x, y, cw, ch)
        if (working !== src) src.recycle()

        if (upscaleToHighRes) {
            val longest = max(working.width, working.height)
            val safeTarget = targetMaxEdge.coerceIn(1080, 3840)
            if (longest < safeTarget) {
                val aspect = working.width.toFloat() / working.height.toFloat()
                val finalW = if (working.width >= working.height) safeTarget else max(1, (safeTarget * aspect).roundToInt())
                val finalH = if (working.height >= working.width) safeTarget else max(1, (safeTarget / aspect).roundToInt())

                // Progressive multi-pass upscaling avoids blocky artifacts on small crops
                while (working.width * 1.75f < finalW && working.height * 1.75f < finalH) {
                    val stepW = (working.width * 1.75f).roundToInt()
                    val stepH = (working.height * 1.75f).roundToInt()
                    val next = Bitmap.createScaledBitmap(working, stepW, stepH, true)
                    if (next !== working) working.recycle()
                    working = next
                }
                if (working.width != finalW || working.height != finalH) {
                    val finalScaled = Bitmap.createScaledBitmap(working, finalW, finalH, true)
                    if (finalScaled !== working) working.recycle()
                    working = finalScaled
                }
            }
        }

        val amount = sharpenAmount.coerceIn(0f, 1f)
        if (amount > 0.05f) {
            val sharpened = applyUnsharpMask(working, amount)
            if (sharpened !== working) working.recycle()
            working = sharpened
        }

        working
    }

    private fun applyUnsharpMask(src: Bitmap, amount: Float): Bitmap {
        val w = src.width
        val h = src.height
        if (w < 3 || h < 3) return src

        val inPx = IntArray(w * h)
        val outPx = IntArray(w * h)
        src.getPixels(inPx, 0, w, 0, 0, w, h)

        val strength = 0.85f * amount
        // Copy border pixels directly
        for (x in 0 until w) {
            outPx[x] = inPx[x]
            outPx[(h - 1) * w + x] = inPx[(h - 1) * w + x]
        }
        for (y in 0 until h) {
            outPx[y * w] = inPx[y * w]
            outPx[y * w + (w - 1)] = inPx[y * w + (w - 1)]
        }

        for (y in 1 until h - 1) {
            val row = y * w
            for (x in 1 until w - 1) {
                val idx = row + x
                val c = inPx[idx]
                val a = (c ushr 24) and 0xFF
                if (a == 0) {
                    outPx[idx] = c
                    continue
                }
                val up = inPx[idx - w]
                val dn = inPx[idx + w]
                val lf = inPx[idx - 1]
                val rt = inPx[idx + 1]

                val cR = (c ushr 16) and 0xFF
                val cG = (c ushr 8) and 0xFF
                val cB = c and 0xFF

                val avgR = (((up ushr 16) and 0xFF) + ((dn ushr 16) and 0xFF) + ((lf ushr 16) and 0xFF) + ((rt ushr 16) and 0xFF)) * 0.25f
                val avgG = (((up ushr 8) and 0xFF) + ((dn ushr 8) and 0xFF) + ((lf ushr 8) and 0xFF) + ((rt ushr 8) and 0xFF)) * 0.25f
                val avgB = ((up and 0xFF) + (dn and 0xFF) + (lf and 0xFF) + (rt and 0xFF)) * 0.25f

                val nR = (cR + (cR - avgR) * strength).roundToInt().coerceIn(0, 255)
                val nG = (cG + (cG - avgG) * strength).roundToInt().coerceIn(0, 255)
                val nB = (cB + (cB - avgB) * strength).roundToInt().coerceIn(0, 255)

                outPx[idx] = (a shl 24) or (nR shl 16) or (nG shl 8) or nB
            }
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(outPx, 0, w, 0, 0, w, h)
        return out
    }
}
