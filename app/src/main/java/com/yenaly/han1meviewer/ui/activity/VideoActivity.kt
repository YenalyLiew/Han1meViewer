package com.yenaly.han1meviewer.ui.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.jzvd.Jzvd
import coil.load
import com.yenaly.han1meviewer.COMMENT_TYPE
import com.yenaly.han1meviewer.Preferences
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
import com.yenaly.han1meviewer.ui.view.video.HMediaKernel
import com.yenaly.han1meviewer.ui.view.video.HanimeDataSource
import com.yenaly.han1meviewer.ui.viewmodel.CommentViewModel
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.OrientationManager
import com.yenaly.yenaly_libs.utils.browse
import com.yenaly.yenaly_libs.utils.intentExtra
import com.yenaly.yenaly_libs.utils.makeBundle
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity
import com.yenaly.yenaly_libs.utils.view.attach
import com.yenaly.yenaly_libs.utils.view.setUpFragmentStateAdapter
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class VideoActivity : YenalyActivity<ActivityVideoBinding, VideoViewModel>(),
    OrientationManager.OrientationChangeListener {

    private val commentViewModel by viewModels<CommentViewModel>()

    private val kernel = HMediaKernel.Type.fromString(Preferences.switchPlayerKernel)

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

        onBackPressedDispatcher.addCallback(this) {
            if (Jzvd.backPress()) {
                return@addCallback
            }
            finish()
        }

        requireNotNull(videoCodeByWebsite ?: videoCode).let {
            viewModel.videoCode = it
            commentViewModel.code = it
            binding.videoPlayer.videoCode = it
        }

        lifecycle.addObserver(OrientationManager(this))
        initViewPager()
        initHKeyframe()
    }

    override fun bindDataObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.hanimeVideoStateFlow.collect { state ->
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
                                    showShortToast(R.string.fail_to_get_video_link)
                                    browse(getHanimeVideoLink(viewModel.videoCode))
                                }
                            } else {
                                binding.videoPlayer.setUp(
                                    HanimeDataSource(state.info.title, state.info.videoUrls),
                                    Jzvd.SCREEN_NORMAL, kernel
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
                            showShortToast(R.string.video_might_not_exist)
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

    override fun onStop() {
        super.onStop()
        Jzvd.goOnPlayOnPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Jzvd.releaseAllVideos()
    }

    override fun onOrientationChanged(orientation: OrientationManager.ScreenOrientation) {
        if (Jzvd.CURRENT_JZVD != null
            && (binding.videoPlayer.state == Jzvd.STATE_PLAYING || binding.videoPlayer.state == Jzvd.STATE_PAUSE)
            && binding.videoPlayer.screen != Jzvd.SCREEN_TINY
            && Jzvd.FULLSCREEN_ORIENTATION != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ) {
            if (orientation.isLandscape && binding.videoPlayer.screen == Jzvd.SCREEN_NORMAL) {
                changeScreenFullLandscape(orientation)
            } else if (orientation === OrientationManager.ScreenOrientation.PORTRAIT
                && binding.videoPlayer.screen == Jzvd.SCREEN_FULLSCREEN
            ) {
                changeScreenNormal()
            }
        }
    }

    /**
     * 竖屏并退出全屏
     */
    private fun changeScreenNormal() {
        if (binding.videoPlayer.screen == Jzvd.SCREEN_FULLSCREEN) {
            binding.videoPlayer.gotoNormalScreen()
        }
    }

    /**
     * 横屏
     */
    private fun changeScreenFullLandscape(orientation: OrientationManager.ScreenOrientation) {
        //从竖屏状态进入横屏
        if (binding.videoPlayer.screen != Jzvd.SCREEN_FULLSCREEN) {
            if (System.currentTimeMillis() - Jzvd.lastAutoFullscreenTime > 2000) {
                binding.videoPlayer.autoFullscreen(orientation)
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
        binding.videoPlayer.onGoHomeClickListener = { _ ->
            // singleTask 直接把所有 VideoActivity 都 finish 掉
            startActivity<MainActivity>()
        }
        binding.videoPlayer.onKeyframeClickListener = { v ->
            binding.videoPlayer.clickHKeyframe(v)
        }
        binding.videoPlayer.onKeyframeLongClickListener = {
            if (!binding.videoPlayer.mediaInterface.isPlaying) {
                val currentPosition = binding.videoPlayer.currentPositionWhenPlaying
                it.context.showAlertDialog {
                    setTitle(R.string.add_to_h_keyframe)
                    setMessage(buildString {
                        appendLine(getString(R.string.sure_to_add_to_h_keyframe))
                        append(getString(R.string.current_position_d_ms, currentPosition))
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
                showShortToast(R.string.pause_then_long_press)
            }
        }
    }
}