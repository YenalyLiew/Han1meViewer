package com.yenaly.han1meviewer.util

import android.widget.Button
import androidx.annotation.DrawableRes

fun Button.setDrawableTop(@DrawableRes drawableRes: Int) {
    this.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0)
}