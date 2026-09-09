package com.palmlens.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.palmlens.core.coroutines.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device capture pipeline (spec §2 step 3, §3.11). CPU-bound → runs on the Default
 * dispatcher, never the main thread. Re-encoding from a decoded [Bitmap] means NO metadata
 * (including GPS) survives — the "strip EXIF GPS" requirement is satisfied for free.
 */
@Singleton
class ImagePreprocessor @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) {

    suspend fun preprocess(
        jpeg: ByteArray,
        cropRect: RectF = FULL,
        maxLongEdge: Int = 1024,
        quality: Int = 85,
    ): ByteArray = withContext(dispatcher) {
        val rotation = readRotation(jpeg)

        var bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
            ?: error("Could not decode captured image")

        if (rotation != 0f) {
            bitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height,
                Matrix().apply { postRotate(rotation) }, true,
            )
        }

        bitmap = crop(bitmap, cropRect)

        val longEdge = max(bitmap.width, bitmap.height)
        if (longEdge > maxLongEdge) {
            val factor = maxLongEdge.toFloat() / longEdge
            bitmap = bitmap.scale((bitmap.width * factor).toInt(), (bitmap.height * factor).toInt())
        }

        ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
    }

    private fun readRotation(jpeg: ByteArray): Float =
        when (
            ExifInterface(ByteArrayInputStream(jpeg))
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

    private fun crop(bitmap: Bitmap, rect: RectF): Bitmap {
        if (rect == FULL) return bitmap
        val left = (rect.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val top = (rect.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val width = (rect.width() * bitmap.width).toInt().coerceIn(1, bitmap.width - left)
        val height = (rect.height() * bitmap.height).toInt().coerceIn(1, bitmap.height - top)
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    companion object {
        val FULL: RectF = RectF(0f, 0f, 1f, 1f)
    }
}
