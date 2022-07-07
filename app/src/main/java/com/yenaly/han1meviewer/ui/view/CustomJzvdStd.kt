package com.yenaly.han1meviewer.ui.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import cn.jzvd.JZDataSource
import cn.jzvd.JzvdStd
import com.yenaly.yenaly_libs.utils.dp

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 15:54
 */
class CustomJzvdStd @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : JzvdStd(context, attrs) {

    override fun setUp(jzDataSource: JZDataSource?, screen: Int) {
        super.setUp(jzDataSource, screen)
        titleTextView.isInvisible = true
        titleTextView.updatePadding(right = 18.dp)
    }

    override fun gotoFullscreen() {
        super.gotoFullscreen()
        titleTextView.isVisible = true
    }

    override fun gotoNormalScreen() {
        super.gotoNormalScreen()
        titleTextView.isInvisible = true
    }

    override fun setScreenNormal() {
        super.setScreenNormal()
        backButton.isVisible = true
    }

    override fun clickBack() {
        Log.i(TAG, "backPress")
        when {
            CONTAINER_LIST.size != 0 && CURRENT_JZVD != null -> { //判断条件，因为当前所有goBack都是回到普通窗口
                CURRENT_JZVD.gotoNormalScreen()
            }
            CONTAINER_LIST.size == 0 && CURRENT_JZVD != null && CURRENT_JZVD.screen != SCREEN_NORMAL -> { //退出直接进入的全屏
                CURRENT_JZVD.clearFloatScreen()
            }
            else -> { //剩餘情況直接退出
                if (context is Activity) (context as Activity).finish()
            }
        }
    }

    override fun onCompletion() {
        if (screen == SCREEN_FULLSCREEN) {
            onStateAutoComplete()
        } else {
            super.onCompletion()
        }
        posterImageView.isGone = true
    }

    fun setVideoSpeed(speed: Float) {
        mediaInterface.setSpeed(speed)
    }
}