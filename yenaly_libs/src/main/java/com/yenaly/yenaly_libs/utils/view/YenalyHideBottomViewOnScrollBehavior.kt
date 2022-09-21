@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.yenaly.yenaly_libs.utils.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewPropertyAnimator
import androidx.annotation.Dimension
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

/**
 * The [CoordinatorLayout.Behavior] for a View within a [CoordinatorLayout] to hide the view off the
 * bottom of the screen when scrolling down, and show it when scrolling up.
 *
 * SPECIFICALLY FOR [BottomNavigationView][com.google.android.material.bottomnavigation.BottomNavigationView].
 *
 * @author Yenaly Liew
 * @time 2022/07/19 019 12:18
 */
open class YenalyHideBottomViewOnScrollBehavior<V : View> : CoordinatorLayout.Behavior<V> {

    private var height = 0
    private var currentState: Int = STATE_SCROLLED_DOWN
    private var additionalHiddenOffsetY = 0
    private var currentAnimator: ViewPropertyAnimator? = null

    constructor() : super()

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs)

    override fun onLayoutChild(
        parent: CoordinatorLayout, child: V, layoutDirection: Int
    ): Boolean {
        val paramsCompat = child.layoutParams as MarginLayoutParams
        height = child.measuredHeight + paramsCompat.bottomMargin
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    /**
     * Sets an additional offset for the y position used to hide the view.
     *
     * @param child the child view that is hidden by this behavior
     * @param offset the additional offset in pixels that should be added when the view slides away
     */
    fun setAdditionalHiddenOffsetY(child: V, @Dimension offset: Int) {
        additionalHiddenOffsetY = offset
        if (currentState == STATE_SCROLLED_DOWN) {
            child.translationY = (height + additionalHiddenOffsetY).toFloat()
        }
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (dyConsumed > 0) {
            slideDown(child)
        } else if (dyConsumed < 0) {
            slideUp(child)
        }
    }

    /** Returns true if the current state is scrolled up.  */
    val isScrolledUp: Boolean
        get() = currentState == STATE_SCROLLED_UP

    /**
     * Performs an animation that will slide the child from it's current position to be totally on the
     * screen.
     */
    @JvmOverloads
    fun slideUp(child: V, animate: Boolean =  /*animate=*/true) {
        if (isScrolledUp) {
            return
        }
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_UP
        val targetTranslationY = 0
        if (animate) {
            animateChildTo(
                child,
                targetTranslationY,
                ENTER_ANIMATION_DURATION.toLong(),
                LINEAR_OUT_SLOW_IN_INTERPOLATOR
            )
        } else {
            child.translationY = targetTranslationY.toFloat()
        }
    }

    /** Returns true if the current state is scrolled down.  */
    val isScrolledDown: Boolean
        get() = currentState == STATE_SCROLLED_DOWN

    /**
     * Performs an animation that will slide the child from it's current position to be totally off
     * the screen.
     */
    @JvmOverloads
    fun slideDown(child: V, animate: Boolean =  /*animate=*/true) {
        if (isScrolledDown) {
            return
        }
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_DOWN
        val targetTranslationY = height + additionalHiddenOffsetY
        if (animate) {
            animateChildTo(
                child,
                targetTranslationY,
                EXIT_ANIMATION_DURATION.toLong(),
                FAST_OUT_LINEAR_IN_INTERPOLATOR
            )
        } else {
            child.translationY = targetTranslationY.toFloat()
        }
    }

    private fun animateChildTo(
        child: V, targetY: Int, duration: Long, interpolator: TimeInterpolator
    ) {
        currentAnimator = child
            .animate()
            .translationY(targetY.toFloat())
            .setInterpolator(interpolator)
            .setDuration(duration)
            .setListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }
                })
    }

    companion object {
        protected const val ENTER_ANIMATION_DURATION = 225
        protected const val EXIT_ANIMATION_DURATION = 175
        private const val STATE_SCROLLED_DOWN = 1
        private const val STATE_SCROLLED_UP = 2

        private val FAST_OUT_LINEAR_IN_INTERPOLATOR: TimeInterpolator =
            FastOutLinearInInterpolator()

        private val LINEAR_OUT_SLOW_IN_INTERPOLATOR: TimeInterpolator =
            LinearOutSlowInInterpolator()
    }
}