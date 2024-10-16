package com.yenaly.yenaly_libs.base.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

open class LongClickableSwitchPreference(
    context: Context, attrs: AttributeSet? = null,
) : MaterialSwitchPreference(context, attrs) {
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