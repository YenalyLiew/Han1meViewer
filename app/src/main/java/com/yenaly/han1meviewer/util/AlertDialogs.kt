package com.yenaly.han1meviewer.util

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

inline fun Context.showAlertDialog(action: MaterialAlertDialogBuilder.() -> Unit) {
    MaterialAlertDialogBuilder(this).apply(action).show()
}

/**
 * Suspends until the user selects a button on the dialog.
 */
suspend fun AlertDialog.await(
    positiveText: String? = null,
    negativeText: String? = null,
    neutralText: String? = null,
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
    show()
}