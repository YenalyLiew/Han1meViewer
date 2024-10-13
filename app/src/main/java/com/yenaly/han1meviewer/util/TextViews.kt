package com.yenaly.han1meviewer.util

import android.content.Context
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.method.LinkMovementMethodCompat
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.getThemeColor
import kotlin.math.roundToInt

private abstract class NoUnderlineClickSpan(val context: Context) : ClickableSpan() {
    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = false
        ds.color = context.getThemeColor(android.R.attr.colorPrimary)
    }
}

fun TextView.setResizableText(
    fullText: String,
    maxLines: Int,
    viewMore: Boolean,
    applyExtraHighlights: ((Spannable) -> (Spannable))? = null,
) {
    val width = width
    if (width <= 0) {
        post {
            setResizableText(fullText, maxLines, viewMore, applyExtraHighlights)
        }
        return
    }
    movementMethod = LinkMovementMethodCompat.getInstance()
    // Since we take the string character by character, we don't want to break up the Windows-style
    // line endings.
    val adjustedText = fullText.replace("\r\n", "\n")
    // Check if even the text has to be resizable.
    val textLayout = StaticLayout.Builder.obtain(
        adjustedText, 0, adjustedText.length, paint,
        width - paddingLeft - paddingRight
    ).setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setIncludePad(includeFontPadding)
        .build()
    if (textLayout.lineCount <= maxLines || adjustedText.isEmpty()) {
        // No need to add 'read more' / 'read less' since the text fits just as well (less than max lines #).
        val htmlText = adjustedText.replace("\n", "<br/>")
        text = addClickablePartTextResizable(
            fullText,
            maxLines,
            HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT),
            null,
            viewMore,
            applyExtraHighlights
        )
        return
    }
    val charactersAtLineEnd = textLayout.getLineEnd(maxLines - 1)
    val suffixText =
        if (viewMore) context.getString(R.string.expand) else context.getString(R.string.collapse)
    var charactersToTake = charactersAtLineEnd - suffixText.length / 2 // Good enough first guess
    if (charactersToTake <= 0) {
        // Happens when text is empty
        val htmlText = adjustedText.replace("\n", "<br/>")
        text = addClickablePartTextResizable(
            fullText,
            maxLines,
            HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT),
            null,
            viewMore,
            applyExtraHighlights
        )
        return
    }
    if (!viewMore) {
        // We can set the text immediately because nothing needs to be measured
        val htmlText = adjustedText.replace("\n", "<br/>")
        text = addClickablePartTextResizable(
            fullText,
            maxLines,
            HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT),
            suffixText,
            false,
            applyExtraHighlights
        )
        return
    }
    val lastHasNewLine =
        adjustedText.substring(
            textLayout.getLineStart(maxLines - 1),
            textLayout.getLineEnd(maxLines - 1)
        )
            .contains("\n")
    val linedText = if (lastHasNewLine) {
        val charactersPerLine =
            textLayout.getLineEnd(0) / (textLayout.getLineWidth(0) / textLayout.ellipsizedWidth.toFloat())
        // non breaking space, will not be thrown away by HTML parser
        val lineOfSpaces = "\u00A0".repeat(charactersPerLine.roundToInt())
        charactersToTake += lineOfSpaces.length - 1
        adjustedText.take(textLayout.getLineStart(maxLines - 1)) +
                adjustedText.substring(
                    textLayout.getLineStart(maxLines - 1),
                    textLayout.getLineEnd(maxLines - 1)
                )
                    .replace("\n", lineOfSpaces) +
                adjustedText.substring(textLayout.getLineEnd(maxLines - 1))
    } else {
        adjustedText
    }
    // Check if we perhaps need to even add characters? Happens very rarely, but can be possible if there was a long word just wrapped
    val shortenedString = linedText.take(charactersToTake)
    val shortenedStringWithSuffix = shortenedString + suffixText
    val shortenedStringWithSuffixLayout = StaticLayout.Builder.obtain(
        shortenedStringWithSuffix, 0, shortenedStringWithSuffix.length, paint,
        width - paddingLeft - paddingRight
    ).setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setIncludePad(includeFontPadding)
        .build()
    val modifier: Int
    if (shortenedStringWithSuffixLayout.getLineEnd(maxLines - 1) >= shortenedStringWithSuffix.length) {
        modifier = 1
        charactersToTake-- // We might just be at the right position already
    } else {
        modifier = -1
    }
    do {
        charactersToTake += modifier
        val baseString = linedText.take(charactersToTake)
        val appended = baseString + suffixText
        val newLayout = StaticLayout.Builder.obtain(
            appended, 0, appended.length, paint,
            width - paddingLeft - paddingRight
        ).setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(includeFontPadding)
            .build()
    } while ((modifier < 0 && newLayout.getLineEnd(maxLines - 1) < appended.length) ||
        (modifier > 0 && newLayout.getLineEnd(maxLines - 1) >= appended.length)
    )
    if (modifier > 0) {
        charactersToTake-- // We went overboard with 1 char, fixing that
    }
    // We need to convert newlines because we are going over to HTML now
    val htmlText = linedText.take(charactersToTake).replace("\n", "<br/>")
    text = addClickablePartTextResizable(
        fullText,
        maxLines,
        HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT),
        suffixText,
        true,
        applyExtraHighlights
    )
}

private fun TextView.addClickablePartTextResizable(
    fullText: String,
    maxLines: Int,
    shortenedText: Spanned,
    clickableText: String?,
    viewMore: Boolean,
    applyExtraHighlights: ((Spannable) -> (Spannable))? = null,
): Spannable {
    val builder = SpannableStringBuilder(shortenedText)
    if (clickableText != null) {
        builder.append(clickableText)
        // val startIndexOffset = if (viewMore) 4 else 0 // Do not highlight the 3 dots and the space
        val startIndexOffset = 0
        builder.setSpan(
            object : NoUnderlineClickSpan(context) {
                override fun onClick(widget: View) {
                    if (viewMore) {
                        setResizableText(fullText, maxLines, false, applyExtraHighlights)
                    } else {
                        setResizableText(fullText, maxLines, true, applyExtraHighlights)
                    }
                }
            },
            builder.indexOf(clickableText) + startIndexOffset,
            builder.indexOf(clickableText) + clickableText.length,
            0
        )
    }
    if (applyExtraHighlights != null) {
        return applyExtraHighlights(builder)
    }
    return builder
}