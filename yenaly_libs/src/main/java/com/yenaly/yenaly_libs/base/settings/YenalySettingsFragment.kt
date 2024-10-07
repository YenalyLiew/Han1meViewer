package com.yenaly.yenaly_libs.base.settings

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.yenaly.yenaly_libs.utils.unsafeLazy

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/17 017 19:26
 * @Description : Description...
 */
abstract class YenalySettingsFragment(@XmlRes private val xmlRes: Int) :
    PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(xmlRes, rootKey)
        initPreferencesVariable()
        onPreferencesCreated(savedInstanceState)
        bindDataObservers()
    }

    override fun setDivider(divider: Drawable?) {
        super.setDivider(null)
    }

    /**
     * 用于绑定数据观察器 (optional)
     */
    open fun bindDataObservers() = Unit

    /**
     * 在此处使用[findPreference]初始化设置中的变量
     */
    open fun initPreferencesVariable() = Unit

    /**
     * 界面与xml设置列表绑定后从此处进行view操作
     */
    abstract fun onPreferencesCreated(savedInstanceState: Bundle?)

    /**
     * 快速獲得隸屬於某[key]的Preference，可以爲null
     */
    fun <T : Preference> preference(key: String) = unsafeLazy { findPreference<T>(key) }

    /**
     * 快速獲得隸屬於某[key]的Preference，不可以爲null
     */
    fun <T : Preference> safePreference(key: String) = unsafeLazy {
        checkNotNull(findPreference<T>(key)) {
            "The preference belonged to the key \"$key\" is null."
        }
    }
}