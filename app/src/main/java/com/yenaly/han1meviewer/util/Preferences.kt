package com.yenaly.han1meviewer.util

import androidx.preference.SeekBarPreference

fun SeekBarPreference.setSummaryConverter(
    defValue: Int,
    converter: (Int) -> CharSequence?,
    action: ((Int) -> Unit)? = null
) {
    setDefaultValue(defValue)
    summary = converter(value)
    setOnPreferenceChangeListener { _, newValue ->
        summary = converter(newValue as Int)
        action?.invoke(newValue)
        true
    }
}