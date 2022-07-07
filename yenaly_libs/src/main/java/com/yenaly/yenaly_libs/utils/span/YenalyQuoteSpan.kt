@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/28 028 22:37
 * @Description : Description...
 */
class YenalyQuoteSpan @JvmOverloads constructor(
    @ColorInt private val color: Int = STANDARD_COLOR,
    private val stripeWidth: Int = STANDARD_STRIPE_WIDTH_PX,
    private val gapWidth: Int = STANDARD_GAP_WIDTH_PX
) : LeadingMarginSpan {

    /**
     * Get the color of the quote stripe.
     *
     * @return the color of the quote stripe.
     */
    @ColorInt
    fun getColor() = this.color

    /**
     * Get the width of the quote stripe.
     *
     * @return the width of the quote stripe.
     */
    fun getStripeWidth() = this.stripeWidth

    /**
     * Get the width of the gap between the stripe and the text.
     *
     * @return the width of the gap between the stripe and the text.
     */
    fun getGapWidth() = this.gapWidth

    override fun getLeadingMargin(first: Boolean): Int {
        return stripeWidth + gapWidth
    }

    override fun drawLeadingMargin(
        c: Canvas,
        p: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout
    ) {
        val style = p.style
        val color = p.color

        p.style = Paint.Style.FILL
        p.color = this.color

        c.drawRect(
            x.toFloat(),
            top.toFloat(),
            (x + dir * this.stripeWidth).toFloat(),
            bottom.toFloat(),
            p
        )

        p.style = style
        p.color = color
    }

    companion object {
        /**
         * Default stripe width in pixels.
         */
        const val STANDARD_STRIPE_WIDTH_PX = 2

        /**
         * Default gap width in pixels.
         */
        const val STANDARD_GAP_WIDTH_PX = 2

        /**
         * Default color for the quote stripe.
         */
        @ColorInt
        const val STANDARD_COLOR = -0xffff01
    }
}