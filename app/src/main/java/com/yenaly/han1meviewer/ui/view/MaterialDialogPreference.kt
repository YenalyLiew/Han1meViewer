package com.yenaly.han1meviewer.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/06 006 22:37
 */
class MaterialDialogPreference : ListPreference {

    constructor(
        context: Context,
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?,
    ) : super(context, attrs) {
        dialog = MaterialAlertDialogBuilder(context)
    }

    private val dialog: MaterialAlertDialogBuilder

    override fun onClick() {
        dialog.setTitle(title)
        dialog.setSingleChoiceItems(entries, findIndexOfValue(value)) { di, which ->
            val str = entryValues[which].toString()
            if (callChangeListener(str)) {
                this.value = str
                di.dismiss()
            }
        }
        dialog.show()
    }
}