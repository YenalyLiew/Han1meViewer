package com.yenaly.han1meviewer.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZDataSource
import cn.jzvd.JZMediaSystem
import cn.jzvd.JZUtils
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
import com.yenaly.han1meviewer.ui.adapter.HKeyframeRvAdapter
import com.yenaly.han1meviewer.ui.adapter.VideoSpeedAdapter
import com.yenaly.han1meviewer.util.notNull
import com.yenaly.han1meviewer.util.removeItself
import com.yenaly.han1meviewer.util.setStateViewLayout
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import java.util.Timer
import kotlin.math.abs

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 15:54
 */
class HJzvdStd @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : JzvdStd(context, attrs), OnLongClickListener {

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

        const val DEF_COUNTDOWN_SEC = 10

        /**
         * 默認速度
         */
        const val DEF_SPEED = 1.0F

        /**
         * 默認速度的索引
         */
        const val DEF_SPEED_INDEX = 2

        /**
         * 默認長按速度是原先速度的幾倍
         */
        const val DEF_LONG_PRESS_SPEED_TIMES = 2.5F

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
    }

    /**
     * 用戶定義的是否顯示底部進度條
     */
    private val showBottomProgress = Preferences.showBottomProgress

    /**
     * 用戶定義的默認速度
     */
    private val userDefSpeed = Preferences.playerSpeed

    /**
     * 用戶定義的默認速度的索引
     */
    private val userDefSpeedIndex = speedArray.indexOfFirst { it == userDefSpeed }

    /**
     * 用戶定義的滑動調整進度條的靈敏度
     */
    private val userDefSlideSensitivity = Preferences.slideSensitivity.toRealSensitivity()

    /**
     * 用戶定義的默認長按速度是原先速度的幾倍
     */
    private val userDefLongPressSpeedTimes = Preferences.longPressSpeedTime

    /**
     * 用戶定義的倒數提醒毫秒數
     */
    private val userDefWhenCountdownRemind = Preferences.whenCountdownRemind

    /**
     * 用戶定義的是否在倒數時顯示評論
     */
    private val userDefShowCommentWhenCountdown = Preferences.showCommentWhenCountdown

    /**
     * 用戶定義的是否啟用關鍵H幀
     */
    private val isHKeyframeEnabled = Preferences.hKeyframesEnable

    /**
     * 當前速度的索引，如果设置速度的话，修改这个，别动 [videoSpeed]
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
            // #issue-14: 有些机器到这里可能会报空指针异常，所以加了个判断，但是不知道为什么会报空指针异常
            if (jzDataSource.objects == null) {
                jzDataSource.objects = arrayOf(userDefSpeedIndex)
            }
            jzDataSource.objects[0] = value
        }

    private lateinit var tvSpeed: TextView
    private lateinit var tvKeyframe: TextView
    private lateinit var tvTimer: TextView

    var hKeyframe: HKeyframeEntity? = null
        set(value) {
            field = value
            hKeyframeAdapter.submitList(value?.keyframes)
            hKeyframeAdapter.isLocal = value?.let { it.author == null } ?: true
        }

    var videoCode: String? = null

    private val hKeyframeAdapter: HKeyframeRvAdapter by unsafeLazy { initHKeyframeAdapter() }

    /**
     * 初始化關鍵H幀的 Adapter，最好不用 lazy
     *
     * 但我還是最終用了 lazy，要不然首次 submitList 收不到
     */
    private fun initHKeyframeAdapter() = run {
        val videoCode = checkNotNull(this.videoCode) {
            "If you want to use HKeyframeAdapter, you must set videoCode first."
        }
        HKeyframeRvAdapter(videoCode).apply {
            setOnItemClickListener { _, _, position ->
                val keyframe = getItem(position).notNull()
                mediaInterface.seekTo(keyframe.position)
                startProgressTimer()
            }
        }
    }

    /**
     * 關鍵H幀的點擊事件
     *
     * 作用：打開 Dialog，顯示關鍵H幀的列表
     */
    var onKeyframeClickListener: ((View) -> Unit)? = null

    /**
     * 關鍵H幀的長按事件
     *
     * 作用：將當前時刻加入關鍵H幀
     */
    var onKeyframeLongClickListener: ((View) -> Unit)? = null

    private var videoSpeed: Float = userDefSpeed
        set(value) {
            field = value
            val isPlaying = mediaInterface.isPlaying
            mediaInterface.setSpeed(value)
            if (!isPlaying) {
                mediaInterface.pause()
            }
        }

    /**
     * 是否觸發了長按快進
     */
    private var isSpeedGestureDetected = false

    /**
     * 長按快進檢測
     */
    // #issue-20: 长按倍速功能添加
    private val speedGestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                when (e.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        if (mediaInterface.isPlaying) {
                            if (setSpeedInternal(videoSpeed * userDefLongPressSpeedTimes)) {
                                isSpeedGestureDetected = true
                            } else {
                                showShortToast(R.string.long_press_speed_not_supported)
                            }
                        }
                    }
                }
            }
        })

    override fun getLayoutId() = R.layout.layout_jzvd_with_speed

    override fun init(context: Context?) {
        super.init(context)
        tvSpeed = findViewById(R.id.tv_speed)
        tvKeyframe = findViewById(R.id.tv_keyframe)
        tvTimer = findViewById(R.id.tv_timer)
        tvSpeed.setOnClickListener(this)
        tvKeyframe.setOnClickListener(this)
        tvKeyframe.setOnLongClickListener(this)
    }

    override fun setUp(jzDataSource: JZDataSource?, screen: Int) {
        super.setUp(jzDataSource, screen, HJZMediaSystem::class.java)
        Log.d("CustomJzvdStd-Settings", buildString {
            append("showBottomProgress: ")
            appendLine(showBottomProgress)
            append("userDefSpeed: ")
            appendLine(userDefSpeed)
            append("userDefSpeedIndex: ")
            appendLine(userDefSpeedIndex)
            append("userDefSlideSensitivity: ")
            appendLine(userDefSlideSensitivity)
        })
        titleTextView.isInvisible = true
        if (bottomProgressBar != null && !showBottomProgress) {
            bottomProgressBar.removeItself()
            bottomProgressBar = ProgressBar(context)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (v.id) {
            R.id.surface_container -> {
                speedGestureDetector.onTouchEvent(event)
                when (event.action) {
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        if (isSpeedGestureDetected) {
                            setSpeedInternal(videoSpeed)
                            isSpeedGestureDetected = false
                        }
                    }
                }
            }
        }
        return super.onTouch(v, event)
    }

    override fun onStatePreparingPlaying() {
        super.onStatePreparingPlaying()
        if (jzDataSource.objects == null) {
            jzDataSource.objects = arrayOf(userDefSpeedIndex)
            currentSpeedIndex = userDefSpeedIndex
        } else {
            currentSpeedIndex = jzDataSource.objects.first() as Int
        }
    }

    override fun setScreenNormal() {
        super.setScreenNormal()
        backButton.isVisible = true
        tvSpeed.isVisible = false
        tvKeyframe.isVisible = false
        titleTextView.isInvisible = true
        tvTimer.isInvisible = true
    }

    override fun setScreenFullscreen() {
        super.setScreenFullscreen()
        tvSpeed.isVisible = true
        if (isHKeyframeEnabled) tvKeyframe.isVisible = true
        titleTextView.isVisible = true
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
            R.id.tv_speed -> clickSpeed()
            R.id.tv_keyframe -> onKeyframeClickListener?.invoke(v)
        }
    }

    override fun onLongClick(v: View): Boolean {
        return when (v.id) {
            R.id.tv_keyframe -> {
                onKeyframeLongClickListener?.invoke(v)
                return true
            }

            else -> false
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
        if (screen != SCREEN_TINY && !isSpeedGestureDetected) {
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
//            mDownY = y;
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

    // 原來是 300 period 我改成了 100 爲了計時準確
    override fun startProgressTimer() {
        Log.i(TAG, "startProgressTimer: " + " [" + this.hashCode() + "] ")
        cancelProgressTimer()
        UPDATE_PROGRESS_TIMER = Timer()
        mProgressTimerTask = ProgressTimerTask()
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 100)
    }

    override fun onProgress(progress: Int, position: Long, duration: Long) {
        super.onProgress(progress, position, duration)
        if (screen == SCREEN_FULLSCREEN) hKeyframe?.let {
            var match = false
            for ((index, kf) in it.keyframes.withIndex()) {
                val interval = kf.position - position
                if (interval in 0L until userDefWhenCountdownRemind) {
                    val timeLong = interval / 1_000L
                    val spannable = spannable {
                        if (userDefShowCommentWhenCountdown) {
                            "#${index + 1}".span {
                                relativeSize(proportion = 0.7F)
                            }
                            if (!kf.prompt.isNullOrBlank()) {
                                " ${kf.prompt}".span {
                                    relativeSize(proportion = 0.7F)
                                }
                            }
                            newline()
                        }
                        val time = if (timeLong >= 1) {
                            (timeLong + 1).toString()
                        } else {
                            val timeFloat = interval / 1_000F
                            "%.1f".format(timeFloat)
                        }
                        time.span {
                            style(Typeface.BOLD)
                        }
                    }
                    tvTimer.text = spannable
                    match = true
                    break
                }
            }
            tvTimer.isInvisible = !match
        } ?: run { tvTimer.isInvisible = true }
    }

    // #issue-14: 之前用 XPopup 三键模式下会有 bug，无法呼出，所以换成这个
    @SuppressLint("InflateParams")
    fun clickSpeed() {
        onCLickUiToggleToClear()
        val inflater = LayoutInflater.from(context).inflate(R.layout.jz_layout_speed, null)
        val rv = inflater.findViewById<RecyclerView>(R.id.rv_video_speed)
        val popup = PopupWindow(
            inflater, JZUtils.dip2px(jzvdContext, 240f),
            LayoutParams.MATCH_PARENT, true
        ).apply {
            contentView = inflater
            animationStyle = cn.jzvd.R.style.pop_animation
        }
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = VideoSpeedAdapter(currentSpeedIndex).apply {
            setOnItemClickListener { _, _, position ->
                currentSpeedIndex = position
                popup.dismiss()
            }
        }
        popup.showAtLocation(textureViewContainer, Gravity.END, 0, 0)
    }

    @SuppressLint("InflateParams")
    fun clickHKeyframe(v: View) {
        onCLickUiToggleToClear()
        val inflater = LayoutInflater.from(context).inflate(R.layout.jz_layout_speed, null)
        val rv = inflater.findViewById<RecyclerView>(R.id.rv_video_speed)
        val popup = PopupWindow(
            inflater, JZUtils.dip2px(jzvdContext, 240f),
            LayoutParams.MATCH_PARENT, true
        ).apply {
            contentView = inflater
            animationStyle = cn.jzvd.R.style.pop_animation
        }
        rv.layoutManager = LinearLayoutManager(v.context)
        val adapter = hKeyframeAdapter
        rv.adapter = adapter
        adapter.setStateViewLayout(
            View.inflate(v.context, R.layout.layout_empty_view, null),
            this@HJzvdStd.context.getString(R.string.here_is_empty) + "\n請長按🥵添加關鍵H幀"
        )
        popup.showAtLocation(textureViewContainer, Gravity.END, 0, 0)
    }

    /**
     * 这个 setSpeed 的 bug 太多了，不同机型效果不一定相同，不得不套个 try catch。
     *
     * #issue-28 就是这个问题，如果我在 [HJZMediaSystem] 中 setSpeed 方法里加的判断不起作用，
     * 那么那个机型就先别用这个功能了。
     */
    private fun setSpeedInternal(speed: Float): Boolean {
        return try {
            mediaInterface.setSpeed(speed)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * 將靈敏度轉換為實際數值，很多用戶對滑動要求挺高，
     * 靈敏度太高沒人在乎，所以高靈敏度照舊，低靈敏度差別大一點
     */
    private fun @receiver:IntRange(from = 1, to = 9) Int.toRealSensitivity(): Int {
        return when (this) {
            1, 2, 3, 4, 5 -> this
            6 -> 7
            7 -> 10
            8 -> 20
            9 -> 40
            else -> throw IllegalStateException("Invalid sensitivity value: $this")
        }
    }
}

class HJZMediaSystem(jzvd: Jzvd) : JZMediaSystem(jzvd) {

    // #issue-26: 有的手機長按快進會報錯，合理懷疑是不是因爲沒有加 post
    override fun setSpeed(speed: Float) {
        mMediaHandler.post {
            val pp = mediaPlayer.playbackParams
            if (speed >= 0f) {
                pp.speed = speed
                mediaPlayer.playbackParams = pp
            }
        }
    }

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