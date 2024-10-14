package com.yenaly.yenaly_libs.base.frame

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yenaly.yenaly_libs.R
import com.yenaly.yenaly_libs.utils.dp

/**
 * @author : Yenaly Liew
 * @time : 2022/04/16 016 20:25
 */
abstract class FrameFragment : Fragment {

    constructor() : super()
    constructor(@LayoutRes resId: Int) : super(resId)

    val window: Window get() = requireActivity().window

    private lateinit var loadingDialog: AlertDialog

    @Deprecated("狗都不用")
    @JvmOverloads
    open fun showLoadingDialog(
        loadingText: String = getString(R.string.yenaly_loading),
        cancelable: Boolean = false,
        dialogWidth: Int = 260.dp,
        dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    ) {
        val loadingDialogView =
            LayoutInflater.from(activity).inflate(R.layout.yenaly_dialog_loading, null)
        loadingDialogView.findViewById<TextView>(R.id.loading_text).text = loadingText
        loadingDialog = MaterialAlertDialogBuilder(requireContext())
            .setCancelable(cancelable)
            .setView(loadingDialogView)
            .create()
        loadingDialog.show()
        loadingDialog.window?.setLayout(dialogWidth, dialogHeight)
    }

    @Deprecated("狗都不用")
    open fun hideLoadingDialog() {
        if (this::loadingDialog.isInitialized) {
            loadingDialog.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::loadingDialog.isInitialized) {
            loadingDialog.dismiss()
        }
    }

    /**
     * 快捷构建 Menu
     *
     * 使用了最新 API，创建菜单更简单。
     *
     * @param menuRes menuRes。
     * @param clear 是否清除从上一界面尤其是 Activity 继承过来的 Menu Item，默认为 true。
     * @param action 和 [onOptionsItemSelected] 用法一致。
     */
    open fun addMenu(
        @MenuRes menuRes: Int,
        clear: Boolean = true,
        action: (menuItem: MenuItem) -> Boolean
    ) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // (Optional) 加入 menu.clear() 的原因是，若不加入，如果 Activity 有构建过 Menu，切换到
                // Fragment 会导致 Activity 的 Menu 继承到 Fragment 里，clear 一下
                // 可以使得该 Fragment 能使用自己唯一的 Menu.
                // Fragment 之间的 Menu 问题只需要通过指定 lifecycle 就能搞定，用下面的方法。
                if (clear) menu.clear()
                menuInflater.inflate(menuRes, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return action.invoke(menuItem)
            }
        })
    }

    /**
     * 详情 [addMenu]，最好使用该带 lifecycleOwner 的方法。
     */
    open fun addMenu(
        @MenuRes menuRes: Int,
        owner: LifecycleOwner,
        clear: Boolean = true,
        action: (menuItem: MenuItem) -> Boolean
    ) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (clear) menu.clear()
                menuInflater.inflate(menuRes, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return action.invoke(menuItem)
            }
        }, owner)
    }

    /**
     * 详情 [addMenu]
     */
    open fun addMenu(
        @MenuRes menuRes: Int,
        owner: LifecycleOwner,
        state: Lifecycle.State,
        clear: Boolean = true,
        action: (menuItem: MenuItem) -> Boolean
    ) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (clear) menu.clear()
                menuInflater.inflate(menuRes, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return action.invoke(menuItem)
            }
        }, owner, state)
    }

    /**
     * 清除 Menu 所有元素
     */
    open fun clearMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)
    }
}