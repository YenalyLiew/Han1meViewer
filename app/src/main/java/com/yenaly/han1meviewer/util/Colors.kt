package com.yenaly.han1meviewer.util

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import androidx.annotation.ColorInt

fun colorTransition(
    @ColorInt fromColor: Int,
    @ColorInt toColor: Int,
    action: (ValueAnimator.() -> Unit)? = null,
) {
    val colorAnimation: ValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
    action?.invoke(colorAnimation)

    colorAnimation.start()
}

fun @receiver:ColorInt Int.toColorStateList() = ColorStateList.valueOf(this)