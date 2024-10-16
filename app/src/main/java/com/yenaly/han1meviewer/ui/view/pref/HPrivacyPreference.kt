package com.yenaly.han1meviewer.ui.view.pref

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.text.parseAsHtml
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.util.createAlertDialog
import com.yenaly.han1meviewer.util.showWithBlurEffect
import com.yenaly.yenaly_libs.base.preference.LongClickableSwitchPreference

/**
 * 隐私设置视图
 *
 * @since 2024/10/16
 */
class HPrivacyPreference(
    context: Context,
    attrs: AttributeSet? = null
) : LongClickableSwitchPreference(context, attrs) {

    val privacyDialog = createAnalyticsDialog(context).apply {
        setOnShowListener {
            // support link click
            val ad = it as AlertDialog
            val anchorView = ad.getButton(AlertDialog.BUTTON_POSITIVE)
            val contentView = anchorView.rootView as ViewGroup
            contentView.findViewById<TextView>(android.R.id.message).apply {
                movementMethod = LinkMovementMethodCompat.getInstance()
            }

            // set click listener
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (callChangeListener(true)) {
                    isChecked = true
                }
                ad.dismiss()
            }
            ad.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                if (callChangeListener(false)) {
                    isChecked = false
                }
                ad.dismiss()
            }
        }
    }

    override fun onClick() {
        if (isChecked) {
            privacyDialog.showWithBlurEffect()
        } else {
            super.onClick()
        }
    }

    private fun createAnalyticsDialog(context: Context): AlertDialog {
        return context.createAlertDialog {
            setTitle(R.string.about_analytics)
            setMessage(context.getString(R.string.about_analytics_summary).parseAsHtml())
            setPositiveButton(R.string.ok, null)
            setNeutralButton(R.string.deny, null)
        }
    }
}
