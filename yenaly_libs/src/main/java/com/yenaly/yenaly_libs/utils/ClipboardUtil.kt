@file:JvmName("ClipboardUtil")

package com.yenaly.yenaly_libs.utils

import android.content.ClipData
import android.content.ClipboardManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 将文字复制到剪切板
 *
 * @param text    要复制的文字
 * @param label   为此文字设置的用户可见的标签 (optional)
 */
fun copyTextToClipboard(
    text: CharSequence?,
    label: CharSequence? = null,
) {
    val clipboardManager = applicationContext.getSystemService<ClipboardManager>()
    val clipData = ClipData.newPlainText(label, text)
    clipboardManager?.setPrimaryClip(clipData)
}

@JvmSynthetic
fun CharSequence?.copyToClipboard(label: CharSequence? = null) = copyTextToClipboard(this, label)

val textsFromClipboard: Sequence<CharSequence?>
    get() = sequence {
        val context = applicationContext
        val clipboardManager = context.getSystemService<ClipboardManager>()
        val clipData = clipboardManager?.primaryClip ?: return@sequence
        for (i in 0..<clipData.itemCount) {
            clipData.getItemAt(i)?.coerceToText(context)?.let { str ->
                yield(str)
            }
        }
    }

/**
 * 剪贴板中最近一次的内容
 *
 * @return 剪贴板中最近一次的内容
 */
val textFromClipboard: CharSequence?
    get() {
        val context = applicationContext
        val clipboardManager = context.getSystemService<ClipboardManager>()
        val clipData = clipboardManager?.primaryClip ?: return null
        if (clipData.itemCount > 0) {
            clipData.getItemAt(0)?.let { item ->
                return item.coerceToText(context)
            }
        }
        return null
    }

/**
 * 清除剪切板内容
 */
fun clearClipboard() {
    val clipboardManager = applicationContext.getSystemService<ClipboardManager>()
    val clipData = ClipData.newPlainText(null, null)
    clipboardManager?.setPrimaryClip(clipData)
}

/**
 * 监听剪切板内容变化
 */
fun clipboardFlow(distinct: Boolean): Flow<Sequence<CharSequence?>> {
    return callbackFlow {
        val clipboardManager = applicationContext.getSystemService<ClipboardManager>()
        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            trySend(textsFromClipboard)
        }
        clipboardManager?.addPrimaryClipChangedListener(listener)
        awaitClose { clipboardManager?.removePrimaryClipChangedListener(listener) }
    }.run {
        if (distinct) {
            distinctUntilChanged { old, new ->
                val oldIterator = old.iterator()
                val newIterator = new.iterator()
                while (oldIterator.hasNext() && newIterator.hasNext()) {
                    if (oldIterator.next() != newIterator.next()) {
                        return@distinctUntilChanged false
                    }
                }
                oldIterator.hasNext() == newIterator.hasNext()
            }
        } else {
            this
        }
    }
}