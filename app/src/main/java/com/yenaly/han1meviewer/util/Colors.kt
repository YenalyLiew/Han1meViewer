package com.yenaly.han1meviewer.util

import android.content.res.ColorStateList
import androidx.annotation.ColorInt

fun @receiver:ColorInt Int.toColorStateList() = ColorStateList.valueOf(this)