package com.yenaly.han1meviewer.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.IntRange
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.view.pref.MaterialDialogPreference
import com.yenaly.han1meviewer.ui.view.video.HJzvdStd
import com.yenaly.han1meviewer.ui.view.video.HMediaKernel
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/09/04 004 16:28
 */
class PlayerSettingsFragment : YenalySettingsFragment(R.xml.settings_player),
    IToolbarFragment<SettingsActivity> {

    companion object {
        const val SWITCH_PLAYER_KERNEL = "switch_player_kernel"
        const val SHOW_BOTTOM_PROGRESS = "show_bottom_progress"
        const val PLAYER_SPEED = "player_speed"
        const val SLIDE_SENSITIVITY = "slide_sensitivity"
        const val LONG_PRESS_SPEED_TIMES = "long_press_speed_times"
    }

    private val switchPlayerKernel
            by safePreference<MaterialDialogPreference>(SWITCH_PLAYER_KERNEL)
    private val showBottomProgressPref
            by safePreference<SwitchPreferenceCompat>(SHOW_BOTTOM_PROGRESS)
    private val playerSpeed
            by safePreference<MaterialDialogPreference>(PLAYER_SPEED)
    private val slideSensitivity
            by safePreference<SeekBarPreference>(SLIDE_SENSITIVITY)
    private val longPressSpeedTimesPref
            by safePreference<MaterialDialogPreference>(LONG_PRESS_SPEED_TIMES)

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        switchPlayerKernel.apply {
            val kernelNames = HMediaKernel.Type.entries.map { it.name }.toTypedArray()
            entries = kernelNames
            entryValues = kernelNames
            if (value == null) setValueIndex(HMediaKernel.Type.ExoPlayer.ordinal)
        }
        playerSpeed.apply {
            entries = HJzvdStd.speedStringArray
            entryValues = Array(HJzvdStd.speedArray.size) {
                HJzvdStd.speedArray[it].toString()
            }
            // 不能直接用 defaultValue 设置，没效果
            if (value == null) setValueIndex(HJzvdStd.DEF_SPEED_INDEX)
        }
        slideSensitivity.apply {
            setDefaultValue(HJzvdStd.DEF_PROGRESS_SLIDE_SENSITIVITY)
            summary = value.toPrettySensitivityString()
            setOnPreferenceChangeListener { pref, newVal ->
                pref.summary = (newVal as Int).toPrettySensitivityString()
                return@setOnPreferenceChangeListener true
            }
        }
        longPressSpeedTimesPref.apply {
            entries = arrayOf(
                getString(R.string.d_speed_times, 1f),
                getString(R.string.d_speed_times, 1.5f),
                getString(R.string.d_speed_times, 2f),
                "${getString(R.string.d_speed_times, 2.5f)} (${getString(R.string.default_)})",
                getString(R.string.d_speed_times, 2.8f),
                getString(R.string.d_speed_times, 3f),
                getString(R.string.d_speed_times, 3.2f),
                getString(R.string.d_speed_times, 3.5f),
                getString(R.string.d_speed_times, 3.8f),
                getString(R.string.d_speed_times, 4f)
            )
            entryValues = arrayOf(
                "1", "1.5", "2", "2.5", "2.8",
                "3", "3.2", "3.5", "3.8", "4"
            )
            if (value == null) setValueIndex(3)
        }
    }

    /**
     * 將數字靈敏度轉換為漂亮的字串
     */
    private fun @receiver:IntRange(from = 1, to = 9) Int.toPrettySensitivityString(): String {
        val pretty = when (this) {
            1, 2 -> getString(R.string.high)
            3, 4 -> getString(R.string.moderately_high)
            5 -> getString(R.string.moderate)
            6 -> getString(R.string.slightly_low)
            7 -> getString(R.string.low)
            8 -> getString(R.string.very_low)
            9 -> getString(R.string.extremely_low)
            else -> throw IllegalStateException("Invalid sensitivity value: $this")
        }
        return getString(R.string.current_slide_sensitivity, pretty)
    }

    override fun SettingsActivity.setupToolbar() {
        supportActionBar!!.setTitle(R.string.player_settings)
    }
}