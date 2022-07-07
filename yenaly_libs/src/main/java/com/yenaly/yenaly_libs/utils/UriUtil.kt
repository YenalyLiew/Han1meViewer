@file:JvmName("UriUtil")

package com.yenaly.yenaly_libs.utils

import android.net.Uri
import android.webkit.MimeTypeMap

inline val Uri.fileExtension get() = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

inline val Uri.mimeType get() = application.contentResolver.getType(this)