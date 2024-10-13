package com.yenaly.yenaly_libs.base.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.yenaly.yenaly_libs.R

open class MaterialSwitchPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : SwitchPreferenceCompat(context, attrs) {
    init {
        widgetLayoutResource = R.layout.yenaly_preference_switch_widget
    }

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
        return onPreferenceLongClickListener?.onPreferenceLongClick(this) ?: false
    }

    fun interface OnPreferenceLongClickListener {
        fun onPreferenceLongClick(preference: Preference): Boolean
    }
}