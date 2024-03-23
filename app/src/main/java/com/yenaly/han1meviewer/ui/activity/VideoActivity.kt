package com.yenaly.han1meviewer.ui.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import coil.load
import com.yenaly.han1meviewer.COMMENT_TYPE
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.VIDEO_COMMENT_PREFIX
import com.yenaly.han1meviewer.databinding.ActivityVideoBinding
import com.yenaly.han1meviewer.getHanimeVideoLink
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.han1meviewer.logic.exception.ParseException
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.ui.fragment.video.CommentFragment
import com.yenaly.han1meviewer.ui.fragment.video.VideoIntroductionFragment
import com.yenaly.han1meviewer.ui.viewmodel.CommentViewModel
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.ScreenRotateUtil
import com.yenaly.yenaly_libs.utils.browse
import com.yenaly.yenaly_libs.utils.intentExtra
import com.yenaly.yenaly_libs.utils.makeBundle
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.view.attach
import com.yenaly.yenaly_libs.utils.view.setUpFragmentStateAdapter
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class VideoActivity : YenalyActivity<ActivityVideoBinding, VideoViewModel>(),
    ScreenRotateUtil.OrientationChangeListener {

    companion object {
        /**
         * 用於保存當前正在播放的VideoActivity
         */
        val currentVideoActivitySet = linkedSetOf<VideoActivity>()
    }

    private val commentViewModel by viewModels<CommentViewModel>()

    private val videoCode by intentExtra<String>(VIDEO_CODE)
    private var videoTitle: String? = null
    private var videoCodeByWebsite: String? = null

    private val tabNameArray = intArrayOf(R.string.introduction, R.string.comment)

    override fun initData(savedInstanceState: Bundle?) {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            uri?.let {
                videoCodeByWebsite = it.getQueryParameter("v")
            }
        }

        currentVideoActivitySet += this
        Log.d("CurrentVideoActivitySet", "$this was added.")

        (videoCodeByWebsite ?: videoCode!!).let {
            viewModel.videoCode = it
            commentViewModel.code = it
            binding.videoPlayer.videoCode = it
        }

        ScreenRotateUtil.getInstance(this).setOrientationChangeListener(this)
        initViewPager()
        initHKeyframe()
    }

    override fun bindDataObservers() {
        lifecycleScope.launch {
            whenStarted {
                viewModel.hanimeVideoFlow.collect { state ->
                    when (state) {
                        is VideoLoadingState.Error -> {
                            showShortToast(state.throwable.localizedMessage)
                            if (state.throwable is ParseException) {
                                browse(getHanimeVideoLink(viewModel.videoCode))
                            }
                            finish()
                        }

                        is VideoLoadingState.Loading -> {

                        }

                        is VideoLoadingState.Success -> {
                            viewModel.csrfToken = state.info.csrfToken
                            videoTitle = state.info.title

                            if (state.info.videoUrls.isEmpty()) {
                                binding.videoPlayer.startButton.setOnClickListener {
                                    showShortToast("無法得到該影片的播放連接，即將轉向瀏覽器")
                                    browse(getHanimeVideoLink(viewModel.videoCode))
                                }
                            } else {
                                binding.videoPlayer.setUp(
                                    JZDataSource(state.info.videoUrls, state.info.title),
                                    Jzvd.SCREEN_NORMAL
                                )
                            }
                            binding.videoPlayer.posterImageView.load(state.info.coverUrl) {
                                crossfade(true)
                            }
                            // 將觀看記錄保存數據庫
                            val entity = WatchHistoryEntity(
                                state.info.coverUrl,
                                state.info.title,
                                state.info.uploadTimeMillis,
                                Clock.System.now().toEpochMilliseconds(),
                                viewModel.videoCode
                            )
                            viewModel.insertWatchHistory(entity)
                        }

                        is VideoLoadingState.NoContent -> {
                            // todo: 有時間轉移到 strings.xml
                            showShortToast("可能該影片不存在")
                            finish()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.observeKeyframe(viewModel.videoCode)?.flowWithLifecycle(lifecycle)?.collect {
                binding.videoPlayer.hKeyframe = it
                viewModel.hKeyframes = it
            }
        }

        lifecycleScope.launch {
            viewModel.modifyHKeyframeFlow.collect { (_, reason) ->
                showShortToast(reason)
            }
        }
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        ScreenRotateUtil.getInstance(this).start(this)
    }

    override fun onStop() {
        super.onStop()
        Jzvd.goOnPlayOnPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 可能需要把ScreenRotateUtil的listener置null，但置null可能会崩溃
        ScreenRotateUtil.getInstance(this).stop()
        Jzvd.releaseAllVideos()
        currentVideoActivitySet -= this
        Log.d("CurrentVideoActivitySet", "$this was removed.")
    }

    override fun orientationChange(orientation: Int) {
        if (Jzvd.CURRENT_JZVD != null
            && (binding.videoPlayer.state == Jzvd.STATE_PLAYING || binding.videoPlayer.state == Jzvd.STATE_PAUSE)
            && binding.videoPlayer.screen != Jzvd.SCREEN_TINY
            && Jzvd.FULLSCREEN_ORIENTATION != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ) {
            if (orientation in 45..315 && binding.videoPlayer.screen == Jzvd.SCREEN_NORMAL) {
                changeScreenFullLandscape(ScreenRotateUtil.orientationDirection)
            } else if (((orientation in 0 until 45) || orientation > 315) && binding.videoPlayer.screen == Jzvd.SCREEN_FULLSCREEN) {
                changeScreenNormal()
            }
        }
    }

    /**
     * 竖屏并退出全屏
     */
    private fun changeScreenNormal() {
        if (binding.videoPlayer.screen == Jzvd.SCREEN_FULLSCREEN) {
            binding.videoPlayer.autoQuitFullscreen()
        }
    }

    /**
     * 横屏
     */
    private fun changeScreenFullLandscape(x: Float) {
        //从竖屏状态进入横屏
        if (binding.videoPlayer.screen != Jzvd.SCREEN_FULLSCREEN) {
            if (System.currentTimeMillis() - Jzvd.lastAutoFullscreenTime > 2000) {
                binding.videoPlayer.autoFullscreen(x)
                Jzvd.lastAutoFullscreenTime = System.currentTimeMillis()
            }
        }
    }

    private fun initViewPager() {

        binding.videoVp.setUpFragmentStateAdapter(this) {
            addFragment { VideoIntroductionFragment() }
            addFragment { CommentFragment().makeBundle(COMMENT_TYPE to VIDEO_COMMENT_PREFIX) }
        }

        binding.videoTl.attach(binding.videoVp) { tab, position ->
            tab.setText(tabNameArray[position])
        }
    }

    private fun initHKeyframe() {
        binding.videoPlayer.onKeyframeClickListener = { v ->
            binding.videoPlayer.clickHKeyframe(v)
        }
        binding.videoPlayer.onKeyframeLongClickListener = {
            if (!binding.videoPlayer.mediaInterface.isPlaying) {
                val currentPosition = binding.videoPlayer.currentPositionWhenPlaying
                it.context.showAlertDialog {
                    setTitle("加入關鍵H幀")
                    setMessage(buildString {
                        appendLine("確定要將當前時刻加入關鍵H幀嗎？")
                        append("當前時刻：${currentPosition}ms")
                    })
                    setPositiveButton(R.string.confirm) { _, _ ->
                        viewModel.appendHKeyframe(
                            viewModel.videoCode,
                            videoTitle ?: "Untitled",
                            HKeyframeEntity.Keyframe(
                                position = currentPosition,
                                prompt = null // 這裏不要給太多負擔，保存就行了沒必要寫comment
                            )
                        )
                    }
                    setNegativeButton(R.string.cancel, null)
                }
            } else {
                showShortToast("先暫停再長按")
            }
        }
    }
}