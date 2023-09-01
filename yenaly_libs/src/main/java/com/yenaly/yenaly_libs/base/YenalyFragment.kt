package com.yenaly.yenaly_libs.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.yenaly.yenaly_libs.base.frame.FrameFragment
import java.lang.reflect.ParameterizedType

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/16 016 20:25
 * @Description : Description...
 */
abstract class YenalyFragment<DB : ViewDataBinding, VM : ViewModel> @JvmOverloads constructor(
    private val sharedViewModel: Boolean = true,
    private val viewModelFactory: ViewModelProvider.Factory? = null
) : FrameFragment() {

    lateinit var binding: DB
    lateinit var viewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
        if (::binding.isInitialized) {
            binding.unbind()
        }
    }

    private fun initView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {
        binding = getViewBinding(inflater, container)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = if (sharedViewModel) {
            createViewModel(this, requireActivity(), viewModelFactory)
        } else {
            createViewModel(this, this, viewModelFactory)
        }
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
    private fun <DB : ViewDataBinding> getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DB {
        val dbClass =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<DB>
        val inflate = dbClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        return inflate.invoke(null, inflater, container, false) as DB
    }

    @Suppress("unchecked_cast")
    private fun <VM : ViewModel> createViewModel(
        fragment: Fragment,
        viewModelStoreOwner: ViewModelStoreOwner,
        factory: ViewModelProvider.Factory? = null,
    ): VM {
        val vmClass =
            (fragment.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VM>
        Log.d("vmClass", vmClass.toString())
        return if (factory != null) {
            ViewModelProvider(viewModelStoreOwner, factory)[vmClass]
        } else {
            ViewModelProvider(viewModelStoreOwner)[vmClass]
        }
    }
}