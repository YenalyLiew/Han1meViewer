package com.yenaly.han1meviewer.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.activity

/**
 * 极其简易的路由器，用于从任何地方跳转到设置界面
 */
class SettingsRouter private constructor(
    private val context: Context,
    private val navController: NavController
) {
    companion object {
        const val DESTINATION = "destination"
        const val BUNDLE = "bundle"

        /**
         * 适用于 fragment 内部跳转
         */
        @JvmStatic
        fun with(fragment: Fragment) = SettingsRouter(
            fragment.requireContext(),
            fragment.findNavController()
        )

        /**
         * 适用于 activity 跳转
         */
        @JvmStatic
        fun with(navController: NavController) = SettingsRouter(
            navController.context,
            navController
        )
    }

    /**
     * 依靠 intent extra 运作，只适用于其他 activity 打开设置界面
     */
    fun navigateFromActivity(
        args: Bundle? = null,
        inclusive: Boolean = false
    ) {
        val activity = context.activity ?: return
        val id = activity.intent.getIntExtra(DESTINATION, 0)
        if (id == 0) return
        navController.navigate(id, args, navOptions {
            popUpTo(R.id.homeSettingsFragment) {
                this.inclusive = true
            }
            anim {
                enter = R.anim.fade_in
                exit = R.anim.fade_out
                popEnter = R.anim.fade_in
                popExit = R.anim.fade_out
            }
        }.takeIf {
            inclusive
        })
    }

    fun toSettingsActivity(@IdRes id: Int = 0, bundle: Bundle? = null) {
        val intent = Intent(context, SettingsActivity::class.java).apply {
            putExtra(DESTINATION, id)
            bundle?.let {
                putExtra(BUNDLE, it)
            }
        }
        context.startActivity(intent)
    }

    /**
     * 适用于 Settings 内不同设置页面跳转
     */
    fun navigateWithinSettings(
        @IdRes to: Int,
        args: Bundle? = null,
        inclusive: Boolean = false
    ) {
        val from = navController.currentDestination?.id ?: return
        navController.navigate(to, args, navOptions {
            popUpTo(from) {
                this.inclusive = inclusive
            }
            anim {
                enter = R.anim.fade_in
                exit = R.anim.fade_out
                popEnter = R.anim.fade_in
                popExit = R.anim.fade_out
            }
        })
    }
}