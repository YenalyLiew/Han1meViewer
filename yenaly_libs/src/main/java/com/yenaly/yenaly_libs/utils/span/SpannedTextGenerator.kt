@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils.span

import android.graphics.BlurMaskFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/27 027 16:28
 * @Description : Description...
 */
class SpannedTextGenerator private constructor() {

    class KotlinBuilder(private val flag: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {

        private val lineSeparator = System.getProperty("line.separator")

        private val ssb = SpannableStringBuilder()

        private var start = 0
        private var end = 0

        fun addText(
            text: CharSequence,
            url: String? = null,
            isNewLine: Boolean = true,
            @Px textSize: Int = -1,
            relativeSize: Float = -1F,
            @Px firstLineMarginStart: Int = 0,
            @Px restLineMarginStart: Int = 0,
            scaleX: Float = -1F,
            @ColorInt backgroundColor: Int? = null,
            @ColorInt foregroundColor: Int? = null,
            @ColorInt quoteColor: Int = YenalyQuoteSpan.STANDARD_COLOR,
            @Px quoteStripeWidth: Int = YenalyQuoteSpan.STANDARD_STRIPE_WIDTH_PX,
            @Px quoteGapWidth: Int = YenalyQuoteSpan.STANDARD_GAP_WIDTH_PX,
            blurRadius: Float = -1F,
            blurStyle: BlurMaskFilter.Blur = BlurMaskFilter.Blur.NORMAL,
            isBold: Boolean = false,
            isItalic: Boolean = false,
            isUnderLine: Boolean = false,
            isStrikeThrough: Boolean = false,
            isSuperscript: Boolean = false,
            isSubscript: Boolean = false,
            isQuote: Boolean = false,
            onClick: OnClickListener? = null
        ): KotlinBuilder {

            start = ssb.length
            ssb.append(text)
            end = ssb.length

            if (textSize >= 0) {
                ssb.setSpan(AbsoluteSizeSpan(textSize), start, end, flag)
            }
            if (relativeSize >= 0F) {
                ssb.setSpan(RelativeSizeSpan(relativeSize), start, end, flag)
            }
            ssb.setSpan(
                LeadingMarginSpan.Standard(firstLineMarginStart, restLineMarginStart),
                start,
                end,
                flag
            )
            if (scaleX >= 0F) {
                ssb.setSpan(ScaleXSpan(scaleX), start, end, flag)
            }
            if (backgroundColor != null) {
                ssb.setSpan(BackgroundColorSpan(backgroundColor), start, end, flag)
            }
            if (foregroundColor != null) {
                ssb.setSpan(ForegroundColorSpan(foregroundColor), start, end, flag)
            }
            if (blurRadius >= 0F) {
                ssb.setSpan(MaskFilterSpan(BlurMaskFilter(blurRadius, blurStyle)), start, end, flag)
            }

            if (isBold) {
                ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, flag)
            }
            if (isItalic) {
                ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, flag)
            }
            if (isUnderLine) {
                ssb.setSpan(UnderlineSpan(), start, end, flag)
            }
            if (isStrikeThrough) {
                ssb.setSpan(StrikethroughSpan(), start, end, flag)
            }
            if (isSuperscript) {
                ssb.setSpan(SuperscriptSpan(), start, end, flag)
            }
            if (isSubscript) {
                ssb.setSpan(SubscriptSpan(), start, end, flag)
            }
            if (isQuote) {
                ssb.setSpan(
                    YenalyQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth),
                    start,
                    end,
                    flag
                )
            }
            if (isNewLine) {
                ssb.append(lineSeparator)
            }
            url?.let {
                ssb.setSpan(URLSpan(it), start, end, flag)
            }
            onClick?.let {
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        it.onClick(widget, url, text)
                    }
                }
                ssb.setSpan(clickableSpan, start, end, flag)
            }

            return this
        }

        fun addNewLine(@Px textSize: Int = -1): KotlinBuilder {
            addText(lineSeparator as CharSequence, textSize = textSize, isNewLine = false)
            return this
        }

        fun addImage(
            @DrawableRes resId: Int,
            verticalAlignment: Int = YenalyImageSpan.ALIGN_MIDDLE,
            @Px width: Int = -1,
            @Px height: Int = -1,
            scaleX: Float = 1F,
            scaleY: Float = 1F,
            marginLeft: Int = 0,
            marginRight: Int = 0,
            fontWidthMultiple: Float = -1F,
            isNewLine: Boolean = false
        ): KotlinBuilder {
            val drawable = ContextCompat.getDrawable(applicationContext, resId)
            addImage(
                drawable,
                verticalAlignment,
                width,
                height,
                scaleX,
                scaleY,
                marginLeft,
                marginRight,
                fontWidthMultiple,
                isNewLine
            )
            return this
        }

        fun addImage(
            drawable: Drawable?,
            verticalAlignment: Int = YenalyImageSpan.ALIGN_MIDDLE,
            @Px width: Int = -1,
            @Px height: Int = -1,
            scaleX: Float = 1F,
            scaleY: Float = 1F,
            marginLeft: Int = 0,
            marginRight: Int = 0,
            fontWidthMultiple: Float = -1F,
            isNewLine: Boolean = false
        ): KotlinBuilder {
            val start = ssb.length
            ssb.append("[image]")
            val end = ssb.length

            drawable?.let {
                val boundWidth = if (width >= 0) width else it.intrinsicWidth
                val boundHeight = if (height >= 0) height else it.intrinsicHeight
                val scaleWidth = (boundWidth * scaleX).toInt()
                val scaleHeight = (boundHeight * scaleY).toInt()
                it.setBounds(0, 0, scaleWidth, scaleHeight)
                ssb.setSpan(
                    YenalyImageSpan(
                        it,
                        verticalAlignment,
                        fontWidthMultiple,
                        marginLeft,
                        marginRight
                    ), start, end, flag
                )
            }

            if (isNewLine) {
                ssb.append(lineSeparator)
            }
            return this
        }

        fun getText() = ssb

        fun showIn(textView: TextView): KotlinBuilder {

            if (textView.movementMethod == null) {
                textView.movementMethod = LinkMovementMethod.getInstance()
            }

            textView.text = ssb

            return this
        }
    }

    class JavaBuilder(private val flag: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {

        private val lineSeparator = System.getProperty("line.separator")

        private val ssb = SpannableStringBuilder()

        private var text: CharSequence = ""

        private var url: String? = null

        @Px
        private var textSize: Int = -1

        private var relativeSize: Float = -1F

        @Px
        private var firstLineMarginStart: Int = 0

        @Px
        private var restLineMarginStart: Int = 0
        private var scaleX: Float = -1F

        @ColorInt
        private var backgroundColor: Int? = null

        @ColorInt
        private var foregroundColor: Int? = null

        @ColorInt
        private var quoteColor: Int = YenalyQuoteSpan.STANDARD_COLOR

        @Px
        private var quoteStripeWidth: Int = YenalyQuoteSpan.STANDARD_STRIPE_WIDTH_PX

        @Px
        private var quoteGapWidth: Int = YenalyQuoteSpan.STANDARD_GAP_WIDTH_PX
        private var blurRadius: Float = -1F
        private var blurStyle: BlurMaskFilter.Blur = BlurMaskFilter.Blur.NORMAL

        private var isBold: Boolean = false
        private var isItalic: Boolean = false
        private var isUnderLine: Boolean = false
        private var isStrikeThrough: Boolean = false
        private var isSuperscript: Boolean = false
        private var isSubscript: Boolean = false
        private var isQuote: Boolean = false

        private var onClick: OnClickListener? = null

        fun addText(text: CharSequence): JavaBuilder {
            if (this.text.isNotEmpty()) setSpan()
            this.text = text
            return this
        }

        fun addTextLine(text: CharSequence): JavaBuilder {
            if (this.text.isNotEmpty()) setSpan()
            val sb = StringBuilder(text)
            this.text = sb.append(lineSeparator)
            return this
        }

        fun bold(): JavaBuilder {
            isBold = true
            return this
        }

        fun italic(): JavaBuilder {
            isItalic = true
            return this
        }

        fun underline(): JavaBuilder {
            isUnderLine = true
            return this
        }

        fun strikeThrough(): JavaBuilder {
            isStrikeThrough = true
            return this
        }

        fun superscript(): JavaBuilder {
            isSuperscript = true
            return this
        }

        fun subscript(): JavaBuilder {
            isSubscript = true
            return this
        }

        fun quote(): JavaBuilder {
            isQuote = true
            return this
        }

        @JvmOverloads
        fun blur(blurRadius: Float, blurStyle: BlurMaskFilter.Blur = this.blurStyle): JavaBuilder {
            this.blurRadius = blurRadius
            this.blurStyle = blurStyle
            return this
        }

        fun textSize(@Px size: Int): JavaBuilder {
            this.textSize = size
            return this
        }

        fun relativeSize(proportion: Float): JavaBuilder {
            this.relativeSize = proportion
            return this
        }

        fun scaleX(proportion: Float): JavaBuilder {
            this.scaleX = proportion
            return this
        }

        fun firstLineMarginStart(margin: Int): JavaBuilder {
            this.firstLineMarginStart = margin
            return this
        }

        fun restLineMarginStart(margin: Int): JavaBuilder {
            this.restLineMarginStart = margin
            return this
        }

        fun backgroundColor(@ColorInt color: Int): JavaBuilder {
            this.backgroundColor = color
            return this
        }

        fun foregroundColor(@ColorInt color: Int): JavaBuilder {
            this.foregroundColor = color
            return this
        }

        fun quoteStripeWidth(width: Int): JavaBuilder {
            this.quoteStripeWidth = width
            return this
        }

        fun quoteGapWidth(width: Int): JavaBuilder {
            this.quoteGapWidth = width
            return this
        }

        fun url(url: String): JavaBuilder {
            this.url = url
            return this
        }

        fun onClick(onClick: OnClickListener): JavaBuilder {
            this.onClick = onClick
            return this
        }

        private fun setSpan() {
            val start = ssb.length
            ssb.append(text)
            val end = ssb.length

            if (textSize >= 0) {
                ssb.setSpan(AbsoluteSizeSpan(textSize), start, end, flag)
                textSize = -1
            }
            if (relativeSize >= 0F) {
                ssb.setSpan(RelativeSizeSpan(relativeSize), start, end, flag)
                relativeSize = -1F
            }
            ssb.setSpan(
                LeadingMarginSpan.Standard(firstLineMarginStart, restLineMarginStart),
                start,
                end,
                flag
            )
            if (scaleX >= 0F) {
                ssb.setSpan(ScaleXSpan(scaleX), start, end, flag)
                scaleX = -1F
            }
            if (backgroundColor != null) {
                ssb.setSpan(BackgroundColorSpan(backgroundColor!!), start, end, flag)
                backgroundColor = null
            }
            if (foregroundColor != null) {
                ssb.setSpan(ForegroundColorSpan(foregroundColor!!), start, end, flag)
                foregroundColor = null
            }
            if (blurRadius >= 0F) {
                ssb.setSpan(MaskFilterSpan(BlurMaskFilter(blurRadius, blurStyle)), start, end, flag)
                blurRadius = -1F
            }
            if (isBold) {
                ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, flag)
                isBold = false
            }
            if (isItalic) {
                ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, flag)
                isItalic = false
            }
            if (isUnderLine) {
                ssb.setSpan(UnderlineSpan(), start, end, flag)
                isUnderLine = false
            }
            if (isStrikeThrough) {
                ssb.setSpan(StrikethroughSpan(), start, end, flag)
                isStrikeThrough = false
            }
            if (isSuperscript) {
                ssb.setSpan(SuperscriptSpan(), start, end, flag)
                isSuperscript = false
            }
            if (isSubscript) {
                ssb.setSpan(SubscriptSpan(), start, end, flag)
                isSubscript = false
            }
            if (isQuote) {
                ssb.setSpan(
                    YenalyQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth),
                    start,
                    end,
                    flag
                )
                isQuote = false
            }
            url?.let {
                ssb.setSpan(URLSpan(it), start, end, flag)
                url = null
            }
            onClick?.let {
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        it.onClick(widget, url, text)
                    }
                }
                ssb.setSpan(clickableSpan, start, end, flag)
            }
        }

        fun getText(): SpannableStringBuilder {
            setSpan()
            return ssb
        }

        fun showIn(textView: TextView): JavaBuilder {
            setSpan()

            if (textView.movementMethod == null) {
                textView.movementMethod = LinkMovementMethod.getInstance()
            }

            textView.text = ssb

            return this
        }
    }

    interface OnClickListener {
        fun onClick(widget: View, url: String?, text: CharSequence)
    }
}