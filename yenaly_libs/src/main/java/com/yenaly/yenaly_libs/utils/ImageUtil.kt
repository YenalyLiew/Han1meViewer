@file:JvmName("ImageUtil")

package com.yenaly.yenaly_libs.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmapOrNull
import java.io.ByteArrayOutputStream
import java.io.File

fun Drawable.toByteArrayOrNull(): ByteArray? {
    return toBitmapOrNull()?.run {
        ByteArrayOutputStream().use { stream ->
            compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}

fun Bitmap.saveTo(
    file: File,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): Boolean {
    return try {
        file.outputStream().buffered().use { stream ->
            compress(format, quality, stream)
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Drawable.saveTo(
    file: File,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): Boolean {
    return toBitmapOrNull()?.saveTo(file, format, quality) == true
}