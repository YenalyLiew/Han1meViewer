package com.yenaly.han1meviewer.ui.view.funcbar

import android.view.View.OnClickListener
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @since 2025/3/11 22:10
 */
data class Hanidokitem(
    @DrawableRes var icon: Int = 0,
    @StringRes var text: Int = 0,
    var viewAction: OnClickListener? = null,
    var subitems: List<Hanidokitem> = emptyList(),
    private val _isBack: Boolean = false,
) {

    val isBack: Boolean
        get() = this._isBack

    infix fun contentEquals(other: Hanidokitem): Boolean = icon == other.icon && text == other.text

    companion object {
        @JvmStatic
        inline fun create(action: Hanidokitem.() -> Unit): Hanidokitem = Hanidokitem().apply(action)
    }
}