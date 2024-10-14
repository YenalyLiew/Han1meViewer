package com.yenaly.yenaly_libs.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.yenaly.yenaly_libs.base.frame.FrameFragment

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/16 016 20:25
 * @Description : Description...
 */
abstract class YenalyFragment<DB : ViewDataBinding> : FrameFragment(), IViewBinding<DB> {

    private var _binding: DB? = null
    override val binding get() = _binding!!
    val bindingOrNull get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        initView(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData(savedInstanceState)
        bindDataObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.unbind()
    }

    private fun initView(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) {
        _binding = getViewBinding(inflater, container)
        binding.lifecycleOwner = viewLifecycleOwner
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