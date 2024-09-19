package com.yenaly.yenaly_libs.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yenaly.yenaly_libs.R
import java.lang.reflect.ParameterizedType

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/05/04 004 14:46
 * @Description : Description...
 */
abstract class YenalyBottomSheetDialogFragment<DB : ViewDataBinding> : BottomSheetDialogFragment() {

    protected lateinit var binding: DB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val layoutInflater = LayoutInflater.from(context)
        binding = getViewBinding(layoutInflater)
        dialog.setContentView(binding.root)
        initData(savedInstanceState, dialog)
        return dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::binding.isInitialized) {
            binding.unbind()
        }
    }

    /**
     * 设置dialog风格 (optional)
     *
     * 默认为透明，需要自己在rootView上添加背景
     */
    open fun setStyle() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.YenalyBottomSheetDialog)
    }

    /**
     * 初始化数据
     */
    abstract fun initData(savedInstanceState: Bundle?, dialog: Dialog)

    /**
     * 简化fragment内唤出该dialog的方式
     */
    fun showIn(fragment: Fragment) {
        val fragmentManager = fragment.requireActivity().supportFragmentManager
        if (fragmentManager.findFragmentByTag(this.javaClass.name) != null) {
            return
        }
        show(fragmentManager, this.javaClass.name)
    }

    /**
     * 简化activity内唤出该dialog的方式
     */
    fun showIn(activity: FragmentActivity) {
        val fragmentManager = activity.supportFragmentManager
        if (fragmentManager.findFragmentByTag(this.javaClass.name) != null) {
            return
        }
        show(fragmentManager, this.javaClass.name)
    }

    @Suppress("unchecked_cast")
    private fun <DB : ViewDataBinding> getViewBinding(
        inflater: LayoutInflater
    ): DB {
        val dbClass =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<DB>
        val inflate = dbClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        return inflate.invoke(null, inflater, null, false) as DB
    }
}