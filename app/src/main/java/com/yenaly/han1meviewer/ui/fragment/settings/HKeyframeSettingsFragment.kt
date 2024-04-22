package com.yenaly.han1meviewer.ui.fragment.settings

import android.os.Bundle
import androidx.annotation.IntRange
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.view.video.HJzvdStd
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/15 015 10:59
 */
class HKeyframeSettingsFragment : YenalySettingsFragment(R.xml.settings_h_keyframe),
    IToolbarFragment<SettingsActivity> {

    companion object {
        const val H_KEYFRAMES_ENABLE = "h_keyframes_enable"
        const val H_KEYFRAME_MANAGE = "h_keyframe_manage"
        const val SHOW_COMMENT_WHEN_COUNTDOWN = "show_comment_when_countdown"
        const val SHARED_H_KEYFRAMES_ENABLE = "shared_h_keyframes_enable"
        const val SHARED_H_KEYFRAMES_USE_FIRST = "shared_h_keyframes_use_first"
        const val SHARED_H_KEYFRAME_MANAGE = "shared_h_keyframe_manage"
        const val WHEN_COUNTDOWN_REMIND = "when_countdown_remind"

        const val H_KEYFRAME_MANAGE_CATEGORY = "h_keyframe_manage_category"
        const val H_KEYFRAME_SHARED_CATEGORY = "shared_h_keyframes_category"
        const val H_KEYFRAME_CUSTOM_CATEGORY = "h_keyframe_custom_category"
    }

    private val hKeyframesEnable
            by safePreference<SwitchPreferenceCompat>(H_KEYFRAMES_ENABLE)
    private val hKeyframeManage
            by safePreference<Preference>(H_KEYFRAME_MANAGE)

    private val sharedHKeyframesEnable
            by safePreference<SwitchPreferenceCompat>(SHARED_H_KEYFRAMES_ENABLE)
    private val sharedHKeyframesUseFirst
            by safePreference<SwitchPreferenceCompat>(SHARED_H_KEYFRAMES_USE_FIRST)
    private val sharedHKeyframesManage
            by safePreference<Preference>(SHARED_H_KEYFRAME_MANAGE)

    private val showCommentWhenCountdown
            by safePreference<SwitchPreferenceCompat>(SHOW_COMMENT_WHEN_COUNTDOWN)
    private val whenCountdownRemind
            by safePreference<SeekBarPreference>(WHEN_COUNTDOWN_REMIND)

    private val manageCategory
            by safePreference<PreferenceCategory>(H_KEYFRAME_MANAGE_CATEGORY)
    private val sharedCategory
            by safePreference<PreferenceCategory>(H_KEYFRAME_SHARED_CATEGORY)
    private val customCategory
            by safePreference<PreferenceCategory>(H_KEYFRAME_CUSTOM_CATEGORY)

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        hKeyframesEnable.apply {
            summary = keyframeTip(isChecked)
            manageCategory.isVisible = isChecked
            sharedCategory.isVisible = isChecked
            customCategory.isVisible = isChecked
            setOnPreferenceChangeListener { _, newValue ->
                val isChecked = newValue as Boolean
                summary = keyframeTip(isChecked)
                manageCategory.isVisible = isChecked
                sharedCategory.isVisible = isChecked
                customCategory.isVisible = isChecked
                return@setOnPreferenceChangeListener true
            }
        }
        hKeyframeManage.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_hKeyframeSettingsFragment_to_hKeyframesFragment)
                return@setOnPreferenceClickListener true
            }
        }
        sharedHKeyframesEnable.apply {
            sharedHKeyframesUseFirst.isVisible = isChecked
            sharedHKeyframesManage.isVisible = isChecked
            setOnPreferenceChangeListener { _, newValue ->
                val isChecked = newValue as Boolean
                sharedHKeyframesUseFirst.isVisible = isChecked
                sharedHKeyframesManage.isVisible = isChecked
                return@setOnPreferenceChangeListener true
            }
        }
        sharedHKeyframesManage.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_hKeyframeSettingsFragment_to_sharedHKeyframesFragment)
                return@setOnPreferenceClickListener true
            }
        }
        whenCountdownRemind.apply {
            setDefaultValue(HJzvdStd.DEF_COUNTDOWN_SEC)
            summary = value.toPrettyCountdownRemindString()
            setOnPreferenceChangeListener { _, newValue ->
                summary = (newValue as Int).toPrettyCountdownRemindString()
                return@setOnPreferenceChangeListener true
            }
        }
    }

    private fun @receiver:IntRange(from = 5, to = 30) Int.toPrettyCountdownRemindString(): String {
        return buildString {
            val countdown = this@toPrettyCountdownRemindString
            append(getString(R.string.will_remind_before_d_seconds, countdown))
            if (countdown == HJzvdStd.DEF_COUNTDOWN_SEC) append(" (${getString(R.string.default_)})")
        }
    }

    private fun keyframeTip(isChecked: Boolean) = if (isChecked) {
        getString(R.string.h_keyframes_enable_tip)
    } else {
        getString(R.string.h_keyframes_disable_tip)
    }

    override fun SettingsActivity.setupToolbar() {
        supportActionBar!!.setTitle(R.string.h_keyframe_settings)
    }
}