package com.yenaly.han1meviewer.ui.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.AttributeSet
import android.util.Log
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import cn.jzvd.JZDataSource
import cn.jzvd.JZMediaSystem
import cn.jzvd.JZUtils
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.dp
import kotlin.math.abs

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 15:54
 */
class CustomJzvdStd @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : JzvdStd(context, attrs) {

    companion object {
        // 相當於重寫了
        const val THRESHOLD = 10

        // 相當於重寫了
        // 5感覺正好，一般大拖動划滑動條，小拖動划屏幕就完事了
        const val PROGRESS_DRAG_RATE = 5f //进度条滑动阻尼系数 越大播放进度条滑动越慢
    }

    override fun setUp(jzDataSource: JZDataSource?, screen: Int) {
        super.setUp(jzDataSource, screen, CustomJZMediaSystem::class.java)
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
                context.activity?.finish()
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

    override fun touchActionMove(x: Float, y: Float) {
        Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ")
        val deltaX = x - mDownX
        var deltaY = y - mDownY
        val absDeltaX = abs(deltaX)
        val absDeltaY = abs(deltaY)
        // 此處進行了修改，未全屏也能調節進度
        if (screen != SCREEN_TINY) {
            //拖动的是NavigationBar和状态栏
            if (mDownX > JZUtils.getScreenWidth(context)
                || mDownY < JZUtils.getStatusBarHeight(context)
            ) {
                return
            }
            if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                    cancelProgressTimer()
                    if (absDeltaX >= THRESHOLD) {
                        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                        // 否则会因为mediaplayer的状态非法导致App Crash
                        if (state != STATE_ERROR) {
                            mChangePosition = true
                            mGestureDownPosition = currentPositionWhenPlaying
                        }
                    } else {
                        //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                        if (mDownX < mScreenHeight * 0.5f) { //左侧改变亮度
                            mChangeBrightness = true
                            val lp = JZUtils.getWindow(context).attributes
                            if (lp.screenBrightness < 0) {
                                try {
                                    mGestureDownBrightness = Settings.System.getInt(
                                        context.contentResolver,
                                        Settings.System.SCREEN_BRIGHTNESS
                                    ).toFloat()
                                    Log.i(
                                        TAG,
                                        "current system brightness: $mGestureDownBrightness"
                                    )
                                } catch (e: SettingNotFoundException) {
                                    e.printStackTrace()
                                }
                            } else {
                                mGestureDownBrightness = lp.screenBrightness * 255
                                Log.i(
                                    TAG,
                                    "current activity brightness: $mGestureDownBrightness"
                                )
                            }
                        } else { //右侧改变声音
                            mChangeVolume = true
                            mGestureDownVolume =
                                mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        }
                    }
                }
            }
        }

        if (mChangePosition) {
            val totalTimeDuration = duration
            mSeekTimePosition =
                (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenWidth * PROGRESS_DRAG_RATE)).toInt()
                    .toLong()
            if (mSeekTimePosition > totalTimeDuration) mSeekTimePosition = totalTimeDuration
            val seekTime = JZUtils.stringForTime(mSeekTimePosition)
            val totalTime = JZUtils.stringForTime(totalTimeDuration)
            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration)
        }

        if (mChangeVolume) {
            deltaY = -deltaY
            val max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val deltaV = (max * deltaY * 3 / mScreenHeight).toInt()
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0)
            //dialog中显示百分比
            val volumePercent =
                (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight).toInt()
            showVolumeDialog(-deltaY, volumePercent)
        }

        if (mChangeBrightness) {
            deltaY = -deltaY
            val deltaV = (255 * deltaY * 3 / mScreenHeight).toInt()
            val params = JZUtils.getWindow(context).attributes
            if ((mGestureDownBrightness + deltaV) / 255 >= 1) { //这和声音有区别，必须自己过滤一下负值
                params.screenBrightness = 1f
            } else if ((mGestureDownBrightness + deltaV) / 255 <= 0) {
                params.screenBrightness = 0.01f
            } else {
                params.screenBrightness = (mGestureDownBrightness + deltaV) / 255
            }
            JZUtils.getWindow(context).attributes = params
            //dialog中显示百分比
            val brightnessPercent =
                (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight).toInt()
            showBrightnessDialog(brightnessPercent)
//                        mDownY = y;
        }
    }

    override fun onStatePreparingChangeUrl() {
        Log.i(TAG, "onStatePreparingChangeUrl " + " [" + this.hashCode() + "] ")
        state = STATE_PREPARING_CHANGE_URL

        // 原方法直接使用下面的方法，會導致全屏切換清晰度返回正常界面時重置影片。
        // 所以重寫，只抄過調用的方法的一部分。
        // releaseAllVideos()
        CURRENT_JZVD?.let {
            it.reset()
            CURRENT_JZVD = null
        }

        startVideo()
    }

    fun setVideoSpeed(speed: Float) {
        mediaInterface.setSpeed(speed)
    }
}

class CustomJZMediaSystem(jzvd: Jzvd) : JZMediaSystem(jzvd) {
    override fun onVideoSizeChanged(mediaPlayer: MediaPlayer?, width: Int, height: Int) {
        super.onVideoSizeChanged(mediaPlayer, width, height)
        val ratio = width.toFloat() / height // > 1 橫屏， < 1 竖屏
        if (ratio > 1) {
            Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}