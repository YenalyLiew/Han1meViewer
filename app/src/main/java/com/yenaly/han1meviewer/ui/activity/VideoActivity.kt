package com.yenaly.han1meviewer.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import coil.load
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.databinding.ActivityVideoBinding
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.ui.fragment.CommentFragment
import com.yenaly.han1meviewer.ui.fragment.VideoIntroductionFragment
import com.yenaly.han1meviewer.ui.viewmodel.CommentViewModel
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.*
import com.yenaly.yenaly_libs.utils.view.attach
import com.yenaly.yenaly_libs.utils.view.setUpFragmentStateAdapter
import kotlinx.coroutines.launch

class VideoActivity : YenalyActivity<ActivityVideoBinding, VideoViewModel>(),
    ScreenRotateUtil.OrientationChangeListener {

    private val commentViewModel by viewModels<CommentViewModel>()

    private val videoCode by intentExtra<String>(VIDEO_CODE)
    private var videoCodeByWebsite: String? = null

    private val tabNameArray = intArrayOf(R.string.introduction, R.string.comment)

    override fun initData(savedInstanceState: Bundle?) {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            uri?.let {
                videoCodeByWebsite = it.getQueryParameter("v")
            }
        }

        viewModel.videoCode = videoCodeByWebsite ?: videoCode!!
        commentViewModel.code = videoCodeByWebsite ?: videoCode!!

        ScreenRotateUtil.getInstance(this).setOrientationChangeListener(this)
        initViewPager()

        getHanimeVideo(viewModel.videoCode)
    }

    override fun liveDataObserve() {
        lifecycleScope.launch {
            whenStarted {
                viewModel.hanimeVideoFlow.collect { state ->
                    when (state) {
                        is VideoLoadingState.Error -> {
                            showShortToast(state.throwable.message)
                            browse(getHanimeVideoLink(videoCodeByWebsite ?: videoCode!!))
                            finish()
                        }
                        is VideoLoadingState.Loading -> {

                        }
                        is VideoLoadingState.Success -> {
                            if (state.info.videoUrls.isEmpty()) {
                                binding.videoPlayer.startButton.setOnClickListener {
                                    showShortToast("????????????????????????????????????????????????????????????")
                                    browse(getHanimeVideoLink(videoCodeByWebsite ?: videoCode!!))
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
                            // ??????????????????????????????
                            val releaseDate = TimeUtil.string2Millis(
                                state.info.uploadTimeWithViews.substringBefore('|').trim(),
                                "yyyy-MM-dd"
                            )
                            val entity = WatchHistoryEntity(
                                state.info.coverUrl, state.info.title,
                                releaseDate, System.currentTimeMillis(),
                                getHanimeVideoLink(videoCodeByWebsite ?: videoCode!!)
                            )
                            viewModel.insertWatchHistory(entity)
                        }
                        is VideoLoadingState.NoContent -> {
                            // todo: ?????????????????? strings.xml
                            showShortToast("????????????????????????")
                            finish()
                        }
                    }
                }
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

    override fun onPause() {
        super.onPause()
        ScreenRotateUtil.getInstance(this).stop()
        Jzvd.releaseAllVideos()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ???????????????ScreenRotateUtil???listener???null?????????null???????????????
    }

    override fun orientationChange(orientation: Int) {
        if (Jzvd.CURRENT_JZVD != null
            && (binding.videoPlayer.state == Jzvd.STATE_PLAYING || binding.videoPlayer.state == Jzvd.STATE_PAUSE)
            && binding.videoPlayer.screen != Jzvd.SCREEN_TINY
        ) {
            if (orientation in 45..315 && binding.videoPlayer.screen == Jzvd.SCREEN_NORMAL) {
                changeScreenFullLandscape(ScreenRotateUtil.orientationDirection)
            } else if (((orientation in 0 until 45) || orientation > 315) && binding.videoPlayer.screen == Jzvd.SCREEN_FULLSCREEN) {
                changeScreenNormal()
            }
        }
    }

    private fun getHanimeVideo(videoCode: String) =
        viewModel.getHanimeVideo(videoCode)

    /**
     * ?????????????????????
     */
    private fun changeScreenNormal() {
        if (binding.videoPlayer.screen == Jzvd.SCREEN_FULLSCREEN) {
            binding.videoPlayer.autoQuitFullscreen()
        }
    }

    /**
     * ??????
     */
    private fun changeScreenFullLandscape(x: Float) {
        //???????????????????????????
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
}