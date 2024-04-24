@file:JvmName("ShareUtil")
@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils

import android.net.Uri
import androidx.core.app.ShareCompat
import com.yenaly.yenaly_libs.ActivityManager

@JvmOverloads
fun shareText(content: CharSequence, title: CharSequence? = null) {
    share("text/plain") {
        setText(content)
        setChooserTitle(title)
    }
}

@JvmOverloads
fun shareImages(imageUris: List<Uri>, title: CharSequence? = null) {
    shareTextAndImages(content = null, imageUri = imageUris, title = title)
}

@JvmOverloads
fun shareTextAndImages(content: CharSequence?, imageUri: List<Uri>, title: CharSequence? = null) {
    share("image/*") {
        setText(content)
        imageUri.forEach(::addStream)
        setChooserTitle(title)
    }
}

@JvmOverloads
fun shareFiles(uris: List<Uri>, title: CharSequence? = null, mimeType: String? = null) {
    share(mimeType ?: uris.firstOrNull()?.mimeType) {
        uris.forEach(::addStream)
        setChooserTitle(title)
    }
}

inline fun share(mimeType: String?, crossinline block: ShareCompat.IntentBuilder.() -> Unit) =
    ShareCompat
        .IntentBuilder(ActivityManager.currentActivity.get() ?: applicationContext)
        .setType(mimeType)
        .apply(block)
        .startChooser()