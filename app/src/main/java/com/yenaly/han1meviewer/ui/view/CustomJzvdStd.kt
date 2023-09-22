package com.yenaly.han1meviewer.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import cn.jzvd.JZDataSource
import cn.jzvd.JZMediaSystem
import cn.jzvd.JZUtils
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.lxj.xpopup.XPopup
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.preferenceSp
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.unsafeLazy
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
        /**
         * 滑动操作的阈值
         */
        const val THRESHOLD = 10

        // 相當於重寫了
        /**
         * 默認滑動調整進度條的靈敏度 越大播放进度条滑动越慢
         */
        const val DEF_PROGRESS_SLIDE_SENSITIVITY = 5

        /**
         * 默認速度
         */
        const val DEF_SPEED = 1.0F

        /**
         * 默認速度的索引
         */
        private const val DEF_SPEED_INDEX = 2

        /**
         * 速度列表
         */
        val speedArray = floatArrayOf(
            0.5F, 0.75F,
            1.0F, 1.25F, 1.5F, 1.75F,
            2.0F, 2.25F, 2.5F, 2.75F,
            3.0F,
        )

        /**
         * 速度列表的字符串
         */
        val speedStringArray = Array(speedArray.size) { "${speedArray[it]}x" }

        /**
         * 用戶定義的是否顯示底部進度條
         */
        var showBottomProgress = preferenceSp.getBoolean("show_bottom_progress", true)

        /**
         * 用戶定義的默認速度
         */
        var userDefSpeed =
            preferenceSp.getString("player_speed", DEF_SPEED.toString())?.toFloat() ?: DEF_SPEED

        /**
         * 用戶定義的默認速度的索引
         */
        var userDefSpeedIndex = speedArray.indexOfFirst { it == userDefSpeed }

        /**
         * 用戶定義的滑動調整進度條的靈敏度
         */
        var userDefSlideSensitivity =
            preferenceSp.getInt("slide_sensitivity", DEF_PROGRESS_SLIDE_SENSITIVITY)
            set(value) {
                field = value
                Log.d("CustomJzvdStd", "userDefSlideSensitivity: $value")
            }
    }

    /**
     * 當前速度的索引
     */
    private var currentSpeedIndex = userDefSpeedIndex
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            if (value == DEF_SPEED_INDEX) {
                tvSpeed.text = "倍速"
            } else {
                tvSpeed.text = speedStringArray[value]
            }
            videoSpeed = speedArray[value]
            jzDataSource.objects[0] = value
        }
    private lateinit var tvSpeed: TextView

    private val speedPopup by unsafeLazy {
        XPopup.Builder(context).atView(tvSpeed).isDarkTheme(true)
            .hasShadowBg(false).hasStatusBar(false)
            .asAttachList(speedStringArray, null) { index, _ ->
                currentSpeedIndex = index
            }
    }

    private var videoSpeed: Float = userDefSpeed
        set(value) {
            field = value
            val isPlaying = mediaInterface.isPlaying
            mediaInterface.setSpeed(value)
            if (!isPlaying) {
                mediaInterface.pause()
            }
        }

    override fun getLayoutId() = R.layout.layout_jzvd_with_speed

    override fun init(context: Context?) {
        super.init(context)
        tvSpeed = findViewById(R.id.tv_speed)
        tvSpeed.setOnClickListener(this)
        if (bottomProgressBar != null && !showBottomProgress) {
            bottomProgressBar.removeItself()
            bottomProgressBar = ProgressBar(context)
        }
    }

    override fun setUp(jzDataSource: JZDataSource?, screen: Int) {
        super.setUp(jzDataSource, screen, CustomJZMediaSystem::class.java)
        titleTextView.isInvisible = true
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
        tvSpeed.isVisible = false
    }

    override fun setScreenFullscreen() {
        super.setScreenFullscreen()
        tvSpeed.isVisible = true
        if (jzDataSource.objects == null) {
            jzDataSource.objects = arrayOf(userDefSpeedIndex)
            currentSpeedIndex = userDefSpeedIndex
        } else {
            currentSpeedIndex = jzDataSource.objects.first() as Int
        }
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

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.tv_speed -> speedPopup.show()
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
                        // 否则会因为media player的状态非法导致App Crash
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
                            if (mAudioManager == null) {
                                mAudioManager = context.getSystemService()
                            }
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
                (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenWidth * userDefSlideSensitivity)).toLong()
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

    override fun showWifiDialog() {
        jzvdContext.showAlertDialog {
            setTitle("Warning!")
            setMessage(cn.jzvd.R.string.tips_not_wifi)
            setPositiveButton(cn.jzvd.R.string.tips_not_wifi_confirm) { _, _ ->
                WIFI_TIP_DIALOG_SHOWED = true
                if (state == STATE_PAUSE) startButton.performClick() else startVideo()
            }
            setNegativeButton(cn.jzvd.R.string.tips_not_wifi_cancel) { _, _ ->
                releaseAllVideos()
                clearFloatScreen()
            }
        }
    }

    /**
     * 刪除自己
     */
    private fun View.removeItself() {
        (parent as? ViewGroup)?.removeView(this)
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