package com.yenaly.han1meviewer.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.IntRange
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.view.CustomJzvdStd
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment
import rikka.preference.SimpleMenuPreference

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/09/04 004 16:28
 */
class PlayerSettingsFragment : YenalySettingsFragment(R.xml.settings_player),
    IToolbarFragment<SettingsActivity> {

    private val showBottomProgressPref by safePreference<SwitchPreferenceCompat>("show_bottom_progress")
    private val playerSpeed by safePreference<SimpleMenuPreference>("player_speed")
    private val slideSensitivity by safePreference<SeekBarPreference>("slide_sensitivity")

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        playerSpeed.apply {
            entries = CustomJzvdStd.speedStringArray
            entryValues = Array(CustomJzvdStd.speedArray.size) {
                CustomJzvdStd.speedArray[it].toString()
            }
            setDefaultValue(CustomJzvdStd.DEF_SPEED.toString())
            setOnPreferenceChangeListener { _, newValue ->
                val newVal = (newValue as String).toFloat()
                CustomJzvdStd.userDefSpeed = newVal
                CustomJzvdStd.userDefSpeedIndex =
                    CustomJzvdStd.speedArray.indexOfFirst { it == newVal }
                return@setOnPreferenceChangeListener true
            }
        }
        slideSensitivity.apply {
            setDefaultValue(CustomJzvdStd.DEF_PROGRESS_SLIDE_SENSITIVITY)
            summary = getString(
                R.string.current_slide_sensitivity,
                value.toPrettySensitivityString()
            )
            setOnPreferenceChangeListener { _, newValue ->
                val newVal = newValue as Int
                CustomJzvdStd.userDefSlideSensitivity = newVal.toRealSensitivity()
                summary = getString(
                    R.string.current_slide_sensitivity,
                    newVal.toPrettySensitivityString()
                )
                return@setOnPreferenceChangeListener true
            }
        }
    }

    /**
     * 將靈敏度轉換為實際數值，很多用戶對滑動要求挺高，
     * 靈敏度太高沒人在乎，所以高靈敏度照舊，低靈敏度差別大一點
     */
    private fun @receiver:IntRange(from = 1, to = 9) Int.toRealSensitivity(): Int {
        return when (this) {
            1, 2, 3, 4, 5 -> this
            6 -> 7
            7 -> 10
            8 -> 20
            9 -> 40
            else -> throw IllegalStateException("Invalid sensitivity value: $this")
        }
    }

    /**
     * 將數字靈敏度轉換為漂亮的字串
     */
    private fun @receiver:IntRange(from = 1, to = 9) Int.toPrettySensitivityString(): String {
        return when (this) {
            1, 2 -> "高"
            3, 4 -> "較高"
            5 -> "適中"
            6 -> "稍低"
            7 -> "較低"
            8 -> "低"
            9 -> "極低"
            else -> throw IllegalStateException("Invalid sensitivity value: $this")
        }
    }

    override fun SettingsActivity.setupToolbar() {
        supportActionBar!!.setTitle(R.string.player_settings)
    }
}