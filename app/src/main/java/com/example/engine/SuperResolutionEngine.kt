package com.example.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class PixelRepairProfile(
    val label: String,
    val labelBn: String,
    val deblockStrength: Float,
    val sharpenStrength: Float,
    val clarityBoost: Float
) {
    BALANCED_HD("Auto Balanced HD", "অটো পিক্সেল HD", 0.55f, 0.75f, 0.28f),
    SMOOTH_PORTRAIT("Smooth Photo", "ফাটা ফটো মসৃণ HD", 0.80f, 0.52f, 0.20f),
    CRISP_TEXT_LOGO("Ultra-Crisp", "লেখা ও লোগো স্পষ্ট", 0.35f, 0.95f, 0.36f)
}

/**
 * Offline Pixel-by-Pixel Super-Resolution Engine:
 * 1. Edge-preserving bilateral de-blocking (removes JPEG/pixel blockiness without blurring contours)
 * 2. Separable 4-tap Catmull-Rom Bicubic sub-pixel reconstruction (eliminates square pixel staircases)
 * 3. Gradient-masked anisotropic sharpening & clarity boost (reconstructs crisp edges even when zoomed 10x)
 */
object SuperResolutionEngine {

    private const val MAX_ENGINE_PIXELS = 12_000_000L // Safe ~12MP high-res cap for mobile RAM

    suspend fun enhanceImageFile(
        imagePath: String,
        targetMaxEdge: Int = 3840,
        profile: PixelRepairProfile = PixelRepairProfile.BALANCED_HD
    ): Bitmap? = withContext(Dispatchers.Default) {
        val src = BitmapFactory.decodeFile(imagePath) ?: return@withContext null
        val result = enhanceBitmapInternal(src, targetMaxEdge, profile)
        if (result !== src) src.recycle()
        result
    }

    suspend fun enhanceBitmap(
        src: Bitmap,
        targetMaxEdge: Int = 3840,
        profile: PixelRepairProfile = PixelRepairProfile.BALANCED_HD
    ): Bitmap = withContext(Dispatchers.Default) {
        enhanceBitmapInternal(src, targetMaxEdge, profile)
    }

    private fun enhanceBitmapInternal(
        src: Bitmap,
        targetMaxEdge: Int,
        profile: PixelRepairProfile
    ): Bitmap {
        val sw = src.width
        val sh = src.height
        if (sw < 2 || sh < 2) return src

        val srcPixels = IntArray(sw * sh)
        src.getPixels(srcPixels, 0, sw, 0, 0, sw, sh)

        val (finalPixels, dw, dh) = enhancePixelBuffer(srcPixels, sw, sh, targetMaxEdge, profile)
        val out = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888)
        out.setPixels(finalPixels, 0, dw, 0, 0, dw, dh)
        return out
    }

    internal fun enhancePixelBuffer(
        initialPixels: IntArray,
        sw: Int,
        sh: Int,
        targetMaxEdge: Int,
        profile: PixelRepairProfile
    ): Triple<IntArray, Int, Int> {
        var srcPixels = initialPixels

        // Stage 1: Edge-preserving bilateral de-blocking on source pixels (cleans blocky/torn compression)
        if (max(sw, sh) <= 2400 && profile.deblockStrength > 0.05f) {
            srcPixels = bilateralDeblock(srcPixels, sw, sh, profile.deblockStrength)
        }

        // Compute target dimensions safely (up to 3840/4096 max edge, keeping total pixels within safe RAM limit)
        val safeMaxEdge = targetMaxEdge.coerceIn(1080, 4096)
        val longest = max(sw, sh)
        val desiredMax = if (longest < safeMaxEdge) {
            min(safeMaxEdge, max(longest * 3, min(safeMaxEdge, 3200)))
        } else {
            longest
        }

        val aspect = sw.toFloat() / sh.toFloat()
        var dw = if (sw >= sh) desiredMax else max(2, (desiredMax * aspect).roundToInt())
        var dh = if (sh >= sw) desiredMax else max(2, (desiredMax / aspect).roundToInt())

        if (dw.toLong() * dh.toLong() > MAX_ENGINE_PIXELS) {
            val factor = sqrt(MAX_ENGINE_PIXELS.toDouble() / (dw.toLong() * dh.toLong()).toDouble()).toFloat()
            dw = max(2, (dw * factor).toInt())
            dh = max(2, (dh * factor).toInt())
        }

        // Stage 2: Separable 4-tap Catmull-Rom Bicubic sub-pixel reconstruction
        val upscaledPixels = if (dw != sw || dh != sh) {
            catmullRomResample(srcPixels, sw, sh, dw, dh)
        } else {
            srcPixels
        }

        // Stage 3: Adaptive gradient-masked edge sharpening + micro-clarity reconstruction
        val finalPixels = edgeDirectedEnhance(
            pixels = upscaledPixels,
            w = dw,
            h = dh,
            sharpen = profile.sharpenStrength,
            clarity = profile.clarityBoost
        )

        return Triple(finalPixels, dw, dh)
    }

    /**
     * Fast 3x3 edge-preserving bilateral filter that smooths blocky artifacts inside flat regions
     * while strictly protecting real edges and alpha boundaries.
     */
    private fun bilateralDeblock(pixels: IntArray, w: Int, h: Int, strength: Float): IntArray {
        val out = IntArray(w * h)
        val rangeSigma = (24f + 46f * strength).coerceIn(16f, 76f)
        val invTwoSigmaSq = 1f / (2f * rangeSigma * rangeSigma)

        for (y in 0 until h) {
            val row = y * w
            val yMin = max(0, y - 1)
            val yMax = min(h - 1, y + 1)
            for (x in 0 until w) {
                val idx = row + x
                val c = pixels[idx]
                val ca = (c ushr 24) and 0xFF
                if (ca == 0) {
                    out[idx] = c
                    continue
                }
                val cr = (c ushr 16) and 0xFF
                val cg = (c ushr 8) and 0xFF
                val cb = c and 0xFF

                var sumW = 1.5f
                var sumR = cr * 1.5f
                var sumG = cg * 1.5f
                var sumB = cb * 1.5f

                for (ny in yMin..yMax) {
                    val nRow = ny * w
                    val xMin = max(0, x - 1)
                    val xMax = min(w - 1, x + 1)
                    for (nx in xMin..xMax) {
                        if (nx == x && ny == y) continue
                        val n = pixels[nRow + nx]
                        val na = (n ushr 24) and 0xFF
                        if (na < 32) continue
                        val nr = (n ushr 16) and 0xFF
                        val ng = (n ushr 8) and 0xFF
                        val nb = n and 0xFF

                        val dr = nr - cr
                        val dg = ng - cg
                        val db = nb - cb
                        val distSq = (dr * dr + dg * dg + db * db).toFloat()
                        val weight = kotlin.math.exp(-distSq * invTwoSigmaSq)
                        sumW += weight
                        sumR += nr * weight
                        sumG += ng * weight
                        sumB += nb * weight
                    }
                }

                val fr = (sumR / sumW).roundToInt().coerceIn(0, 255)
                val fg = (sumG / sumW).roundToInt().coerceIn(0, 255)
                val fb = (sumB / sumW).roundToInt().coerceIn(0, 255)
                out[idx] = (ca shl 24) or (fr shl 16) or (fg shl 8) or fb
            }
        }
        return out
    }

    /**
     * Separable 2-pass 4-tap Catmull-Rom spline resampling (Horizontal -> Vertical).
     * Eliminates blocky nearest/bilinear pixel stairs and creates smooth curves.
     */
    private fun catmullRomResample(
        src: IntArray,
        sw: Int,
        sh: Int,
        dw: Int,
        dh: Int
    ): IntArray {
        val temp = IntArray(dw * sh)
        val scaleX = (sw - 1).toFloat() / max(1, dw - 1).toFloat()

        val xIdx0 = IntArray(dw)
        val xIdx1 = IntArray(dw)
        val xIdx2 = IntArray(dw)
        val xIdx3 = IntArray(dw)
        val xW0 = FloatArray(dw)
        val xW1 = FloatArray(dw)
        val xW2 = FloatArray(dw)
        val xW3 = FloatArray(dw)

        for (dx in 0 until dw) {
            val gx = dx * scaleX
            val ix = gx.toInt()
            val t = gx - ix
            xIdx0[dx] = (ix - 1).coerceIn(0, sw - 1)
            xIdx1[dx] = ix.coerceIn(0, sw - 1)
            xIdx2[dx] = (ix + 1).coerceIn(0, sw - 1)
            xIdx3[dx] = (ix + 2).coerceIn(0, sw - 1)
            val t2 = t * t
            val t3 = t2 * t
            xW0[dx] = -0.5f * t + t2 - 0.5f * t3
            xW1[dx] = 1f - 2.5f * t2 + 1.5f * t3
            xW2[dx] = 0.5f * t + 2f * t2 - 1.5f * t3
            xW3[dx] = -0.5f * t2 + 0.5f * t3
        }

        for (y in 0 until sh) {
            val srcRow = y * sw
            val dstRow = y * dw
            for (dx in 0 until dw) {
                val p0 = src[srcRow + xIdx0[dx]]
                val p1 = src[srcRow + xIdx1[dx]]
                val p2 = src[srcRow + xIdx2[dx]]
                val p3 = src[srcRow + xIdx3[dx]]

                val w0 = xW0[dx]
                val w1 = xW1[dx]
                val w2 = xW2[dx]
                val w3 = xW3[dx]

                val a = (((p0 ushr 24) and 0xFF) * w0 + ((p1 ushr 24) and 0xFF) * w1 + ((p2 ushr 24) and 0xFF) * w2 + ((p3 ushr 24) and 0xFF) * w3).roundToInt().coerceIn(0, 255)
                val r = (((p0 ushr 16) and 0xFF) * w0 + ((p1 ushr 16) and 0xFF) * w1 + ((p2 ushr 16) and 0xFF) * w2 + ((p3 ushr 16) and 0xFF) * w3).roundToInt().coerceIn(0, 255)
                val g = (((p0 ushr 8) and 0xFF) * w0 + ((p1 ushr 8) and 0xFF) * w1 + ((p2 ushr 8) and 0xFF) * w2 + ((p3 ushr 8) and 0xFF) * w3).roundToInt().coerceIn(0, 255)
                val b = ((p0 and 0xFF) * w0 + (p1 and 0xFF) * w1 + (p2 and 0xFF) * w2 + (p3 and 0xFF) * w3).roundToInt().coerceIn(0, 255)
                temp[dstRow + dx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        val out = IntArray(dw * dh)
        val scaleY = (sh - 1).toFloat() / max(1, dh - 1).toFloat()

        for (dy in 0 until dh) {
            val gy = dy * scaleY
            val iy = gy.toInt()
            val t = gy - iy
            val y0 = (iy - 1).coerceIn(0, sh - 1) * dw
            val y1 = iy.coerceIn(0, sh - 1) * dw
            val y2 = (iy + 1).coerceIn(0, sh - 1) * dw
            val y3 = (iy + 2).coerceIn(0, sh - 1) * dw

            val t2 = t * t
            val t3 = t2 * t
            val w0 = -0.5f * t + t2 - 0.5f * t3
            val w1 = 1f - 2.5f * t2 + 1.5f * t3
            val w2 = 0.5f * t + 2f * t2 - 1.5f * t3
            val w3 = -0.5f * t2 + 0.5f * t3

            val dstRow = dy * dw
            for (dx in 0 until dw) {
                val p0 = temp[y0 + dx]
                val p1 = temp[y1 + dx]
                val p2 = temp[y2 + dx]
                val p3 = temp[y3 + dx]

                val a = (((p0 ushr 24) and 0xFF) * w0 + ((p1 ushr 24) and 0xFF) * w1 + ((p2 ushr 24) and 0xFF) * w2 + ((p3 ushr 24) and 0xFF) * w3).roundToInt().coerceIn(0, 255)
                val r = (((p0 ushr 16) and 0xFF) * w0 + ((p1 ushr 16) and 0xFF) * w1 + ((p2 ushr 16) and 0xFF) * w2 + ((p3 ushr 16) and 0xFF) * w3).roundToInt().coerceIn(0, 255)
                val g = (((p0 ushr 8) and 0xFF) * w0 + ((p1 ushr 8) and 0xFF) * w1 + ((p2 ushr 8) and 0xFF) * w2 + ((p3 ushr 8) and 0xFF) * w3).roundToInt().coerceIn(0, 255)
                val b = ((p0 and 0xFF) * w0 + (p1 and 0xFF) * w1 + (p2 and 0xFF) * w2 + (p3 and 0xFF) * w3).roundToInt().coerceIn(0, 255)
                out[dstRow + dx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return out
    }

    /**
     * Adaptive gradient-masked sharpening + micro-clarity:
     * Strong crisp reconstruction along edges/contours, clean non-grainy smoothing in flat areas.
     */
    private fun edgeDirectedEnhance(
        pixels: IntArray,
        w: Int,
        h: Int,
        sharpen: Float,
        clarity: Float
    ): IntArray {
        if (w < 3 || h < 3) return pixels
        val out = IntArray(w * h)
        for (x in 0 until w) {
            out[x] = pixels[x]
            out[(h - 1) * w + x] = pixels[(h - 1) * w + x]
        }
        for (y in 0 until h) {
            out[y * w] = pixels[y * w]
            out[y * w + (w - 1)] = pixels[y * w + (w - 1)]
        }

        val maxGain = 1.25f * sharpen.coerceIn(0f, 1.2f)
        val clarityGain = 0.28f * clarity.coerceIn(0f, 1f)

        for (y in 1 until h - 1) {
            val row = y * w
            for (x in 1 until w - 1) {
                val idx = row + x
                val c = pixels[idx]
                val a = (c ushr 24) and 0xFF
                if (a == 0) {
                    out[idx] = c
                    continue
                }
                val up = pixels[idx - w]
                val dn = pixels[idx + w]
                val lf = pixels[idx - 1]
                val rt = pixels[idx + 1]

                val cR = (c ushr 16) and 0xFF
                val cG = (c ushr 8) and 0xFF
                val cB = c and 0xFF

                val upG = (up ushr 8) and 0xFF
                val dnG = (dn ushr 8) and 0xFF
                val lfG = (lf ushr 8) and 0xFF
                val rtG = (rt ushr 8) and 0xFF

                // Local gradient magnitude suppresses noise amplification in flat zones
                val grad = abs(rtG - lfG) + abs(dnG - upG)
                val edgeWeight = ((grad - 6f) / 48f).coerceIn(0.15f, 1f)
                val gain = maxGain * edgeWeight

                val avgR = (((up ushr 16) and 0xFF) + ((dn ushr 16) and 0xFF) + ((lf ushr 16) and 0xFF) + ((rt ushr 16) and 0xFF)) * 0.25f
                val avgG = (upG + dnG + lfG + rtG) * 0.25f
                val avgB = ((up and 0xFF) + (dn and 0xFF) + (lf and 0xFF) + (rt and 0xFF)) * 0.25f

                // Subtle local midtone clarity
                val dR = cR - avgR
                val dG = cG - avgG
                val dB = cB - avgB

                val boostR = cR + dR * gain + (cR - 128f) * clarityGain * 0.25f
                val boostG = cG + dG * gain + (cG - 128f) * clarityGain * 0.25f
                val boostB = cB + dB * gain + (cB - 128f) * clarityGain * 0.25f

                val nR = boostR.roundToInt().coerceIn(0, 255)
                val nG = boostG.roundToInt().coerceIn(0, 255)
                val nB = boostB.roundToInt().coerceIn(0, 255)

                out[idx] = (a shl 24) or (nR shl 16) or (nG shl 8) or nB
            }
        }
        return out
    }
}
