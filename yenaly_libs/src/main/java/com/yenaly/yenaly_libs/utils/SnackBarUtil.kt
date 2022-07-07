@file:JvmName("SnackBarUtil")

package com.yenaly.yenaly_libs.utils

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

@JvmOverloads
inline fun Activity.showSnackBar(
    message: CharSequence,
    length: Int = Snackbar.LENGTH_SHORT,
    view: View = findViewById(android.R.id.content),
    action: Snackbar.() -> Unit = {}
) {
    Snackbar.make(view, message, length).apply(action).show()
}

@JvmOverloads
inline fun Activity.showSnackBar(
    @StringRes message: Int,
    length: Int = Snackbar.LENGTH_SHORT,
    view: View = findViewById(android.R.id.content),
    action: Snackbar.() -> Unit = {}
) {
    Snackbar.make(view, message, length).apply(action).show()
}

@JvmOverloads
inline fun Fragment.showSnackBar(
    message: CharSequence,
    length: Int = Snackbar.LENGTH_SHORT,
    view: View = requireView(),
    action: Snackbar.() -> Unit = {}
) {
    Snackbar.make(view, message, length).apply(action).show()
}

@JvmOverloads
inline fun Fragment.showSnackBar(
    @StringRes message: Int,
    length: Int = Snackbar.LENGTH_SHORT,
    view: View = requireView(),
    action: Snackbar.() -> Unit = {}
) {
    Snackbar.make(view, message, length).apply(action).show()
}


