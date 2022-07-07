package com.yenaly.yenaly_libs.base.settings

import android.os.Bundle
import android.util.Log
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.preference.PreferenceFragmentCompat
import java.lang.reflect.ParameterizedType

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/17 017 19:26
 * @Description : Description...
 */
abstract class YenalySettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(initPreferencesResource(), rootKey)
        initPreferencesVariable()
        onPreferencesCreated(savedInstanceState)
        liveDataObserve()
    }

    /**
     * Livedata监听位 (optional)
     */
    open fun liveDataObserve() {
    }

    /**
     * 初始化xml设置列表
     */
    @XmlRes
    abstract fun initPreferencesResource(): Int

    /**
     * 在此处使用[findPreference]初始化设置中的变量
     */
    abstract fun initPreferencesVariable()

    /**
     * 界面与xml设置列表绑定后从此处进行view操作
     */
    abstract fun onPreferencesCreated(savedInstanceState: Bundle?)

    @Suppress("unchecked_cast")
    private fun <VM : ViewModel> createViewModel(
        fragment: Fragment,
        viewModelStoreOwner: ViewModelStoreOwner,
        factory: ViewModelProvider.Factory? = null,
    ): VM {
        val vmClass =
            (fragment.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
        Log.d("vmClass", vmClass.toString())
        return if (factory != null) {
            ViewModelProvider(viewModelStoreOwner, factory)[vmClass]
        } else {
            ViewModelProvider(viewModelStoreOwner)[vmClass]
        }
    }
}