package com.yenaly.yenaly_libs.base.frame

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yenaly.yenaly_libs.R
import com.yenaly.yenaly_libs.utils.dp

/**
 * @author Yenaly Liew
 * @time 2022/04/16 016 20:25
 */
abstract class FrameActivity : AppCompatActivity() {

    private lateinit var loadingDialog: AlertDialog

    @Deprecated("狗都不用")
    @JvmOverloads
    open fun showLoadingDialog(
        loadingText: String = getString(R.string.yenaly_loading),
        cancelable: Boolean = false,
        dialogWidth: Int = 260.dp,
        dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    ) {
        val loadingDialogView =
            LayoutInflater.from(this).inflate(R.layout.yenaly_dialog_loading, null)
        loadingDialogView.findViewById<TextView>(R.id.loading_text).text = loadingText
        loadingDialog = MaterialAlertDialogBuilder(this)
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

    /**
     * 主题界面风格相关可以在这里设置 (optional)
     */
    open fun setUiStyle() {
    }

    /**
     * 能够监听该 Activity 旗下所有 Fragment 的 onResume 事件
     */
    open val onFragmentResumedListener: ((Fragment) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setUiStyle()
        super.onCreate(savedInstanceState)
        if (onFragmentResumedListener != null) {
            supportFragmentManager.registerFragmentLifecycleCallbacks(object :
                FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                    if (f is NavHostFragment) return
                    onFragmentResumedListener?.invoke(f)
                    Log.d("FrameActivity", "onFragmentResumed: $f")
                }
            }, true)
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
     * @param action 和 [onOptionsItemSelected] 用法一致。
     */
    open fun addMenu(
        @MenuRes menuRes: Int,
        action: (menuItem: MenuItem) -> Boolean,
    ) {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(menuRes, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return action.invoke(menuItem)
            }
        })
    }

    /**
     * 详情 [addMenu]
     */
    open fun addMenu(
        @MenuRes menuRes: Int,
        owner: LifecycleOwner,
        action: (menuItem: MenuItem) -> Boolean,
    ) {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
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
        action: (menuItem: MenuItem) -> Boolean,
    ) {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(menuRes, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return action.invoke(menuItem)
            }
        }, owner, state)
    }
}