package com.yenaly.han1meviewer.util

import android.content.Context
import android.content.DialogInterface
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.dp
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun Context.getDialogDefaultDrawable(): Drawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 32.dp.toFloat()
        setColor(getColor(R.color.per90_dark_red))
    }
}

/**
 * 注意：占用了 setOnDismissListener，
 * 使用时不要忘了这一点！
 */
fun AlertDialog.createDecorBlurEffect(dismissListener: DialogInterface.OnDismissListener? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.activity?.let { activity ->
            activity.window.decorView.setRenderEffect(
                RenderEffect.createBlurEffect(
                    12.dp.toFloat(),
                    12.dp.toFloat(),
                    Shader.TileMode.CLAMP
                )
            )
            setOnDismissListener {
                activity.window.decorView.setRenderEffect(null)
                dismissListener?.onDismiss(it)
            }
        }
    } else {
        setOnDismissListener(dismissListener)
    }
}

inline fun Context.createAlertDialog(action: MaterialAlertDialogBuilder.() -> Unit): AlertDialog {
    val ad = MaterialAlertDialogBuilder(this)
        .setBackground(getDialogDefaultDrawable())
        .apply(action)
        .create()
    return ad
}

@Suppress("NOTHING_TO_INLINE")
inline fun AlertDialog.showWithBlurEffect(dismissListener: DialogInterface.OnDismissListener? = null) {
    createDecorBlurEffect(dismissListener)
    show()
}

inline fun Context.showAlertDialog(
    dismissListener: DialogInterface.OnDismissListener? = null,
    action: MaterialAlertDialogBuilder.() -> Unit
) {
    createAlertDialog(action).showWithBlurEffect(dismissListener)
}

/**
 * Suspends until the user selects a button on the dialog.
 */
suspend fun AlertDialog.await(
    positiveText: String? = null,
    negativeText: String? = null,
    neutralText: String? = null,
    dismissListener: DialogInterface.OnDismissListener? = null,
) = suspendCancellableCoroutine { cont ->
    val listener = DialogInterface.OnClickListener { _, which ->
        when (which) {
            AlertDialog.BUTTON_POSITIVE -> cont.resume(AlertDialog.BUTTON_POSITIVE)
            AlertDialog.BUTTON_NEGATIVE -> cont.resume(AlertDialog.BUTTON_NEGATIVE)
            else -> cont.resume(AlertDialog.BUTTON_NEUTRAL)
        }
    }

    if (positiveText != null) setButton(AlertDialog.BUTTON_POSITIVE, positiveText, listener)
    if (negativeText != null) setButton(AlertDialog.BUTTON_NEGATIVE, negativeText, listener)
    if (neutralText != null) setButton(AlertDialog.BUTTON_NEUTRAL, neutralText, listener)

    // we can either decide to cancel the coroutine if the dialog
    // itself gets cancelled, or resume the coroutine with the
    // value [false]
    setOnCancelListener { cont.cancel() }

    // if we make this coroutine cancellable, we should also close the
    // dialog when the coroutine is cancelled
    cont.invokeOnCancellation { dismiss() }

    // remember to show the dialog before returning from the block,
    // you won't be able to do it after this function is called!
    showWithBlurEffect(dismissListener)
}