@file:Suppress("unused", "deprecation")

package com.yenaly.yenaly_libs.utils

import android.text.Spanned
import androidx.core.text.HtmlCompat

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/27 027 08:17
 * @Description : Description...
 */
@Deprecated("Use [SpannedTextGenerator]")
class HtmlTextGenerator {

    private val stringBuilder = StringBuilder()

    private val boldTextPrefix = "<b>"
    private val boldTextSuffix = "</b>"

    private val italicTextPrefix = "<i>"
    private val italicTextSuffix = "</i>"

    private val underLineTextPrefix = "<u>"
    private val underLineTextSuffix = "</u>"

    private val newLine = "<br/>"
    private val linkText = """
            <a href="%s">%s</a>
        """.trimIndent()

    fun addNewLine(): HtmlTextGenerator {
        stringBuilder.append(newLine)
        return this
    }

    fun addText(
        text: String,
        isBold: Boolean = false,
        isItalic: Boolean = false,
        isUnderLine: Boolean = false
    ): HtmlTextGenerator {
        val tempString = StringBuilder(text)
        if (isBold) {
            tempString.insert(0, boldTextPrefix)
            tempString.append(boldTextSuffix)
        }
        if (isItalic) {
            tempString.insert(0, italicTextPrefix)
            tempString.append(italicTextSuffix)
        }
        if (isUnderLine) {
            tempString.insert(0, underLineTextPrefix)
            tempString.append(underLineTextSuffix)
        }
        stringBuilder.append(tempString)
        return this
    }

    fun addLinkText(
        link: String,
        text: String
    ): HtmlTextGenerator {
        stringBuilder.append(String.format(linkText, link, text))
        return this
    }

    fun getSpannedText(): Spanned {
        return HtmlCompat.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    fun getText(): String {
        return stringBuilder.toString()
    }
}