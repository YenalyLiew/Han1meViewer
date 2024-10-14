package com.yenaly.han1meviewer.util

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * Add an update listener to the [ValueAnimator] and remove it when the [LifecycleOwner] is destroyed.
 */
fun ValueAnimator.addUpdateListener(
    lifecycle: Lifecycle?,
    listener: ValueAnimator.AnimatorUpdateListener?,
) {
    addUpdateListener(listener)
    lifecycle?.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            this@addUpdateListener.removeAllUpdateListeners()
        }
    })
}

fun ValueAnimator.addUpdateListener(
    fragment: Fragment,
    listener: ValueAnimator.AnimatorUpdateListener?
) {
    if (fragment.isDetached || fragment.view == null) return
    addUpdateListener(fragment.viewLifecycleOwner.lifecycle, listener)
}

fun colorTransition(
    @ColorInt fromColor: Int,
    @ColorInt toColor: Int,
    action: (ValueAnimator.() -> Unit)? = null,
) {
    val colorAnimation: ValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
    action?.invoke(colorAnimation)

    colorAnimation.start()
}