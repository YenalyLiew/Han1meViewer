package com.yenaly.yenaly_libs.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.yenaly.yenaly_libs.base.frame.FrameActivity

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/16 016 20:20
 * @Description : Description...
 */
abstract class YenalyActivity<DB : ViewDataBinding> : FrameActivity(), IViewBinding<DB> {

    private var _binding: DB? = null
    override val binding get() = _binding!!
    val bindingOrNull get() = _binding

    /**
     * 取代之前的反射方式，太消耗性能了
     */
    abstract fun getViewBinding(layoutInflater: LayoutInflater): DB

    final override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): DB {
        return getViewBinding(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData(savedInstanceState)
        bindDataObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding?.unbind()
        // 没啥必要
        // _binding = null
    }

    private fun initView() {
        _binding = getViewBinding(layoutInflater, null)
        setContentView(binding.root)
        binding.lifecycleOwner = this
    }

    /**
     * 用于绑定数据观察器 (optional)
     */
    open fun bindDataObservers() = Unit

    /**
     * 初始化数据
     */
    abstract fun initData(savedInstanceState: Bundle?)
}