package com.yenaly.yenaly_libs.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yenaly.yenaly_libs.base.frame.FrameActivity
import java.lang.reflect.ParameterizedType

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/16 016 20:20
 * @Description : Description...
 */
abstract class YenalyActivity<DB : ViewDataBinding, VM : ViewModel>(
    private val viewModelFactory: ViewModelProvider.Factory? = null
) : FrameActivity() {

    lateinit var binding: DB
    lateinit var viewModel: VM


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData(savedInstanceState)
        bindDataObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::binding.isInitialized) {
            binding.unbind()
        }
    }

    private fun initView() {
        binding = getViewBinding(layoutInflater)
        setContentView(binding.root)
        viewModel = createViewModel(this, viewModelFactory)
        binding.lifecycleOwner = this
    }

    /**
     * 用于绑定数据观察器 (optional)
     */
    open fun bindDataObservers() {
    }

    /**
     * 初始化数据
     */
    abstract fun initData(savedInstanceState: Bundle?)

    @Suppress("unchecked_cast")
    private fun <DB : ViewDataBinding> getViewBinding(inflater: LayoutInflater): DB {
        val dbClass =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<DB>
        Log.d("dbClass", dbClass.toString())
        val inflate = dbClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        return inflate.invoke(null, inflater) as DB
    }

    @Suppress("unchecked_cast")
    private fun <VM : ViewModel> createViewModel(
        activity: ComponentActivity,
        factory: ViewModelProvider.Factory? = null,
    ): VM {
        val vmClass =
            (activity.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VM>
        Log.d("vmClass", vmClass.toString())
        return if (factory != null) {
            ViewModelProvider(activity, factory)[vmClass]
        } else {
            ViewModelProvider(activity)[vmClass]
        }
    }
}