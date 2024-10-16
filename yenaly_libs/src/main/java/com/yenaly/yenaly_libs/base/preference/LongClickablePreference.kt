package com.yenaly.yenaly_libs.base.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/25 025 23:26
 */
open class LongClickablePreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    private var onPreferenceLongClickListener: OnPreferenceLongClickListener? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setOnLongClickListener {
            performLongClick()
        }
    }

    fun setOnPreferenceLongClickListener(onPreferenceLongClickListener: OnPreferenceLongClickListener) {
        this.onPreferenceLongClickListener = onPreferenceLongClickListener
    }

    private fun performLongClick(): Boolean {
        if (!isEnabled || !isSelectable) {
            return false
        }
        return onPreferenceLongClickListener?.onPreferenceLongClick(this) == true
    }

    fun interface OnPreferenceLongClickListener {
        fun onPreferenceLongClick(preference: Preference): Boolean
    }
}