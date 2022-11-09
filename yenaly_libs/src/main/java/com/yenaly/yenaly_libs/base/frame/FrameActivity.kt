package com.yenaly.yenaly_libs.base.frame

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.annotation.CallSuper
import androidx.annotation.MenuRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.core.os.BuildCompat.PrereleaseSdkCheck
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yenaly.yenaly_libs.R
import com.yenaly.yenaly_libs.utils.dp
import java.lang.ref.WeakReference

/**
 * @author Yenaly Liew
 * @time 2022/04/16 016 20:25
 */
abstract class FrameActivity : AppCompatActivity() {

    private lateinit var loadingDialog: AlertDialog

    private var onBackInvokedCallback: OnBackInvokedCallback? = null

    /**
     * SDK33 及以上专用，不需要适配就不用管，详情请见 [onBackEvent] 方法
     */
    open var isNeedInterceptBackEvent: Boolean = false

    @JvmOverloads
    open fun showLoadingDialog(
        loadingText: String = getString(R.string.yenaly_loading),
        cancelable: Boolean = false,
        dialogWidth: Int = 260.dp,
        dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT
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

    @PrereleaseSdkCheck
    override fun onCreate(savedInstanceState: Bundle?) {
        setUiStyle()
        super.onCreate(savedInstanceState)
        if (isNeedInterceptBackEvent && BuildCompat.isAtLeastT()) {
            onBackInvokedCallback = OnBackInvokedCallbackInner(this).also {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, it
                )
            }
        }
    }

    @PrereleaseSdkCheck
    override fun onDestroy() {
        super.onDestroy()
        if (this::loadingDialog.isInitialized) {
            loadingDialog.dismiss()
        }
        if (BuildCompat.isAtLeastT()) {
            onBackInvokedCallback?.let(onBackInvokedDispatcher::unregisterOnBackInvokedCallback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal class OnBackInvokedCallbackInner(frameActivity: FrameActivity) :
        OnBackInvokedCallback {
        private val activity = WeakReference(frameActivity)

        override fun onBackInvoked() {
            activity.get()?.apply {
                onBackEvent()
            }
        }
    }

    /**
     * SDK33 之后 deprecate 了 [onBackPressed] 方法，
     * SDK33 后需要调用这个方法执行新逻辑，
     * 之前版本不需要调用，调用 [onBackPressed] 方法即可。
     *
     * 不要忘了把 [isNeedInterceptBackEvent] 设置为 true.
     *
     * [相关链接](https://juejin.cn/post/7105645114760331300)
     */
    @CallSuper
    @Suppress("deprecation")
    open fun onBackEvent() {
        onBackPressed()
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
        action: (menuItem: MenuItem) -> Boolean
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
        action: (menuItem: MenuItem) -> Boolean
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
        action: (menuItem: MenuItem) -> Boolean
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