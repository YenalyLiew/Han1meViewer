package com.yenaly.yenaly_libs.base.frame

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment

/**
 * @author Yenaly Liew
 * @time 2022/04/16 016 20:25
 */
abstract class FrameActivity : AppCompatActivity() {

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