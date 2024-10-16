package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.yenaly.han1meviewer.DATE_CODE
import com.yenaly.han1meviewer.PREVIEW_COMMENT_PREFIX
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivityPreviewBinding
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.pienization
import com.yenaly.han1meviewer.ui.adapter.HanimePreviewNewsRvAdapter
import com.yenaly.han1meviewer.ui.adapter.HanimePreviewTourRvAdapter
import com.yenaly.han1meviewer.ui.view.CenterLinearLayoutManager
import com.yenaly.han1meviewer.ui.viewmodel.PreviewCommentPrefetcher
import com.yenaly.han1meviewer.ui.viewmodel.PreviewViewModel
import com.yenaly.han1meviewer.util.addUpdateListener
import com.yenaly.han1meviewer.util.colorTransition
import com.yenaly.han1meviewer.util.toColorStateList
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.appScreenWidth
import com.yenaly.yenaly_libs.utils.getThemeColor
import com.yenaly.yenaly_libs.utils.startActivity
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.AppBarLayoutStateChangeListener
import com.yenaly.yenaly_libs.utils.view.innerRecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/23 023 16:46
 */
class PreviewActivity : YenalyActivity<ActivityPreviewBinding>() {

    companion object {
        private const val ANIM_DURATION = 300L
        private val animInterpolator = FastOutSlowInInterpolator()
    }

    val viewModel by viewModels<PreviewViewModel>()

    private val dateUtils = DateUtils()
    private val badgeDrawable by unsafeLazy { BadgeDrawable.create(this@PreviewActivity) }

    /**
     * 左右滑动 VP 时，不触发 onScrollStateChanged，防止触发两次 binding.vpNews.setCurrentItem
     * 导致滑动不流畅。
     */
    private var shouldTriggerScroll = false

    private val tourSimplifiedAdapter = HanimePreviewTourRvAdapter()
    private val linearSnapHelper = LinearSnapHelper()
    private val newsAdapter = HanimePreviewNewsRvAdapter()

    private val tourLayoutManager by unsafeLazy {
        object : CenterLinearLayoutManager(this@PreviewActivity) {

            init {
                orientation = HORIZONTAL
                reverseLayout = false
            }

            override fun scrollVerticallyBy(
                dy: Int,
                recycler: RecyclerView.Recycler?,
                state: RecyclerView.State?,
            ): Int {
                if (!binding.vpNews.isInTouchMode) {
                    onScrollWhenInNonTouchMode(dy)
                }
                return super.scrollVerticallyBy(dy, recycler, state)
            }

            private fun onScrollWhenInNonTouchMode(dy: Int) {
                if (dy > 0) {
                    binding.appBar.setExpanded(false, true)
                } else binding.appBar.setExpanded(true, true)
            }
        }
    }

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityPreviewBinding =
        ActivityPreviewBinding.inflate(layoutInflater)

    override fun setUiStyle() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }

    override fun initData(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeActionContentDescription(R.string.back)
        }

        PreviewCommentPrefetcher.here().tag(PreviewCommentPrefetcher.Scope.PREVIEW_ACTIVITY)

        // binding.newsTitle.findViewById<TextView>(R.id.title).setText(R.string.latest_hanime_news)
        // binding.newsTitle.findViewById<TextView>(R.id.sub_title).setText(R.string.introduction)

        dateUtils.current.format(DateUtils.FORMATTED_FORMAT).let { format ->
            viewModel.getHanimePreview(format)
            loadComments(format)
        }

        binding.fabPrevious.setOnClickListener {
            dateUtils.toPrevDate().format(DateUtils.FORMATTED_FORMAT).let { format ->
                viewModel.getHanimePreview(format)
                loadComments(format)
            }
        }
        binding.fabNext.setOnClickListener {
            dateUtils.toNextDate().format(DateUtils.FORMATTED_FORMAT).let { format ->
                viewModel.getHanimePreview(format)
                loadComments(format)
            }
        }

        binding.vpNews.adapter = newsAdapter

        binding.rvTourSimplified.apply {
            layoutManager = tourLayoutManager
            adapter = tourSimplifiedAdapter
            clipToPadding = false
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, _ ->
                val elementWidth = view.resources.getDimension(
                    R.dimen.video_cover_simplified_width_small
                )
                val padding = appScreenWidth / 2f - elementWidth / 2f
                view.updatePadding(left = padding.toInt(), right = padding.toInt())
                WindowInsetsCompat.CONSUMED
            }
            linearSnapHelper.attachToRecyclerView(this)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (shouldTriggerScroll) {
                            val view = linearSnapHelper.findSnapView(tourLayoutManager)
                            val position = view?.let(::getChildAdapterPosition)
                                ?: RecyclerView.NO_POSITION
                            binding.vpNews.setCurrentItem(position, false)
                        }
                        shouldTriggerScroll = true
                    }
                }
            })
        }

        tourSimplifiedAdapter.setOnItemClickListener { _, _, position ->
            binding.vpNews.setCurrentItem(position, false)
            binding.appBar.setExpanded(false, true)
        }

        binding.vpNews.innerRecyclerView?.apply {
            isNestedScrollingEnabled = false
            clipToPadding = false
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                v.updatePadding(bottom = systemBars.bottom)
                WindowInsetsCompat.CONSUMED
            }
        }
        binding.vpNews.offscreenPageLimit = 1
        binding.vpNews.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                shouldTriggerScroll = false
                binding.rvTourSimplified.smoothScrollToPosition(position)
                handleToolbarColor(position)
            }
        })

        initAnimation()

        //binding.srlPreview.setOnRefreshListener {
        //    viewModel.getHanimePreview(dateUtils.current.second)
        //}
    }

    @OptIn(ExperimentalBadgeUtils::class)
    @SuppressLint("SetTextI18n")
    override fun bindDataObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.previewFlow.collect { state ->
                    binding.nsvPreview.isGone = state !is WebsiteState.Success
                    binding.appBar.setExpanded(state is WebsiteState.Success, true)
                    when (state) {
                        is WebsiteState.Error -> {
                            //binding.srlPreview.finishRefresh()
                            supportActionBar?.title = state.throwable.pienization
                        }

                        is WebsiteState.Loading -> {
                            //binding.srlPreview.autoRefresh()
                            binding.fabPrevious.isEnabled = false
                            binding.fabNext.isEnabled = false
                        }

                        is WebsiteState.Success -> {
                            //binding.srlPreview.finishRefresh()
                            binding.vpNews.setCurrentItem(0, false)
                            supportActionBar?.title = getString(
                                R.string.latest_hanime_list_monthly,
                                dateUtils.current.format(DateUtils.NORMAL_FORMAT)
                            )
                            binding.fabPrevious.apply {
                                isVisible = state.info.hasPrevious
                                isEnabled = state.info.hasPrevious
                                text = dateUtils.prevDate.format(DateUtils.NORMAL_FORMAT)
                            }
                            binding.fabNext.apply {
                                isVisible = state.info.hasNext
                                isEnabled = state.info.hasNext
                                text = dateUtils.nextDate.format(DateUtils.NORMAL_FORMAT)
                            }
                            binding.cover.load(state.info.headerPicUrl) {
                                crossfade(true)
                                allowHardware(false)
                                target(
                                    onStart = binding.cover::setImageDrawable,
                                    onError = binding.cover::setImageDrawable,
                                    onSuccess = {
                                        binding.cover.setImageDrawable(it)
                                        it.toBitmapOrNull()?.let(Palette::Builder)?.generate { p ->
                                            p?.let(::handleHeaderPalette)
                                        }
                                    }
                                )
                            }
                            tourSimplifiedAdapter.submitList(state.info.latestHanime)
                            // 有時候 tour 無法順利加載出來，加點延遲反而就好了，哈哈
                            delay(100)
                            handleToolbarColor(0)
                            newsAdapter.submitList(state.info.previewInfo)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            PreviewCommentPrefetcher.here().commentFlow.collectLatest { comments ->
                badgeDrawable.apply {
                    isVisible = comments.isNotEmpty()
                    number = comments.size
                    badgeGravity = BadgeDrawable.TOP_START
                }
                BadgeUtils.attachBadgeDrawable(badgeDrawable, binding.toolbar, R.id.tb_comment)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreviewCommentPrefetcher.bye(PreviewCommentPrefetcher.Scope.PREVIEW_ACTIVITY)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_preview_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.tb_comment -> {
                startActivity<PreviewCommentActivity>(
                    "date" to dateUtils.current.format(DateUtils.NORMAL_FORMAT),
                    DATE_CODE to dateUtils.current.format(DateUtils.FORMATTED_FORMAT)
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadComments(currentFormat: String) {
        PreviewCommentPrefetcher.here()
            .fetch(PREVIEW_COMMENT_PREFIX, currentFormat)
    }

    private fun handleToolbarColor(index: Int) {
        val data = tourSimplifiedAdapter.getItem(index)?.coverUrl
        val request = ImageRequest.Builder(this)
            .data(data)
            .allowHardware(false)
            .target(
                onError = {

                },
                onSuccess = {
                    it.toBitmapOrNull()?.let(Palette::Builder)?.generate { p ->
                        p?.let(::handleToolbarPalette)
                    }
                }
            )
            .build()
        this.imageLoader.enqueue(request)
    }

    private fun handleToolbarPalette(p: Palette) {
        val darkMuted =
            p.darkMutedSwatch?.rgb ?: p.darkVibrantSwatch?.rgb ?: p.lightVibrantSwatch?.rgb
            ?: p.lightMutedSwatch?.rgb ?: Color.BLACK
        colorTransition(
            fromColor = (binding.collapsingToolbar.contentScrim as ColorDrawable).color,
            toColor = ColorUtils.blendARGB(darkMuted, Color.BLACK, 0.3f)
        ) {
            duration = ANIM_DURATION
            interpolator = animInterpolator
            addUpdateListener(lifecycle) {
                val value = it.animatedValue as Int
                binding.collapsingToolbar.setContentScrimColor(value)
            }
        }
        colorTransition(
            fromColor = (binding.llTour.background as? ColorDrawable)?.color ?: Color.TRANSPARENT,
            toColor = darkMuted
        ) {
            duration = ANIM_DURATION
            interpolator = animInterpolator
            addUpdateListener(lifecycle) {
                val value = it.animatedValue as Int
                binding.llTour.setBackgroundColor(value)
            }
        }
    }

    private fun handleHeaderPalette(p: Palette) {
        val colorPrimary = getThemeColor(com.google.android.material.R.attr.colorPrimary)
        val lightVibrant = p.getLightVibrantColor(colorPrimary)
        val per70lightVibrantStateList =
            ColorUtils.setAlphaComponent(lightVibrant, 0xB3).toColorStateList()
        binding.fabPrevious.backgroundTintList = per70lightVibrantStateList
        binding.fabNext.backgroundTintList = per70lightVibrantStateList
        val titleTextColorStateList =
            (p.lightVibrantSwatch?.titleTextColor ?: Color.BLACK).toColorStateList()
        binding.fabPrevious.iconTint = titleTextColorStateList
        binding.fabNext.iconTint = titleTextColorStateList
        binding.fabPrevious.setTextColor(titleTextColorStateList)
        binding.fabNext.setTextColor(titleTextColorStateList)
    }

    private fun initAnimation() {
        binding.appBar.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                when (state) {
                    State.EXPANDED -> {
                        binding.fabPrevious.animate().translationX(0F).setDuration(ANIM_DURATION)
                            .setInterpolator(animInterpolator).start()
                        binding.fabNext.animate().translationX(0F).setDuration(ANIM_DURATION)
                            .setInterpolator(animInterpolator).start()
                    }

                    State.INTERMEDIATE -> {
                        binding.fabPrevious.animate().translationX(-500F).setDuration(ANIM_DURATION)
                            .setInterpolator(animInterpolator).start()
                        binding.fabNext.animate().translationX(500F).setDuration(ANIM_DURATION)
                            .setInterpolator(animInterpolator).start()
                    }

                    State.COLLAPSED -> {

                    }
                }
            }
        })
    }

    /**
     * 单纯给这个用的DateUtils
     */
    private class DateUtils {

        companion object {
            /**
             * 2022/2
             */
            val NORMAL_FORMAT = LocalDateTime.Format {
                year(); char('/'); monthNumber(Padding.NONE)
            }

            /**
             * 202202
             */
            val FORMATTED_FORMAT = LocalDateTime.Format {
                year(); monthNumber()
            }
        }

        // 當前顯示的日期
        var current: LocalDateTime = currentDate
            private set

        val currentDate: LocalDateTime
            get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val prevDate: LocalDateTime
            get() {
                val instant = current.toInstant(TimeZone.currentSystemDefault())
                return instant.minus(1, DateTimeUnit.MONTH, TimeZone.currentSystemDefault())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            }

        val nextDate: LocalDateTime
            get() {
                val instant = current.toInstant(TimeZone.currentSystemDefault())
                return instant.plus(1, DateTimeUnit.MONTH, TimeZone.currentSystemDefault())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            }

        fun toPrevDate(): LocalDateTime {
            current = prevDate
            return current
        }

        fun toNextDate(): LocalDateTime {
            current = nextDate
            return current
        }
    }
}