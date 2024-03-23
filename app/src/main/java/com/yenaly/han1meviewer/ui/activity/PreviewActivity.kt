package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isGone
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.android.material.appbar.AppBarLayout
import com.yenaly.han1meviewer.DATE_CODE
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivityPreviewBinding
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.adapter.HanimePreviewNewsRvAdapter
import com.yenaly.han1meviewer.ui.adapter.HanimePreviewTourRvAdapter
import com.yenaly.han1meviewer.ui.view.CenterLinearLayoutManager
import com.yenaly.han1meviewer.ui.viewmodel.PreviewViewModel
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.appScreenWidth
import com.yenaly.yenaly_libs.utils.dp
import com.yenaly.yenaly_libs.utils.startActivity
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.AppBarLayoutStateChangeListener
import com.yenaly.yenaly_libs.utils.view.innerRecyclerView
import kotlinx.coroutines.delay
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
class PreviewActivity : YenalyActivity<ActivityPreviewBinding, PreviewViewModel>() {

    companion object {
        private const val animDuration = 300L
        private val animInterpolator = FastOutSlowInInterpolator()
    }

    private val dateUtils = DateUtils()

    private val tourSimplifiedAdapter = HanimePreviewTourRvAdapter()
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

    override fun setUiStyle() {
    }

    override fun initData(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeActionContentDescription(R.string.back)
        }

        // binding.newsTitle.findViewById<TextView>(R.id.title).setText(R.string.latest_hanime_news)
        // binding.newsTitle.findViewById<TextView>(R.id.sub_title).setText(R.string.introduction)

        binding.fabPrevious.setOnClickListener {
            viewModel.getHanimePreview(dateUtils.toPrevDate().format(DateUtils.FORMATTED_FORMAT))
        }
        binding.fabNext.setOnClickListener {
            viewModel.getHanimePreview(dateUtils.toNextDate().format(DateUtils.FORMATTED_FORMAT))
        }

        binding.vpNews.adapter = newsAdapter

        binding.rvTourSimplified.apply {
            layoutManager = tourLayoutManager
            adapter = tourSimplifiedAdapter
            addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State,
                ) {
                    val position = parent.getChildViewHolder(view).bindingAdapterPosition
                    if (position == 0 || position == state.itemCount - 1) {
                        val elementWidth = resources.getDimension(
                            R.dimen.video_cover_simplified_width_small
                        )
                        val elementMargin = 4.dp
                        val padding = appScreenWidth / 2f - elementWidth / 2f - elementMargin
                        if (position == 0) {
                            outRect.left = padding.toInt()
                        } else {
                            outRect.right = padding.toInt()
                        }
                    }
                }
            })
        }

        val linearSnapHelper = LinearSnapHelper()
        linearSnapHelper.attachToRecyclerView(binding.rvTourSimplified)

        tourSimplifiedAdapter.setOnItemClickListener { _, _, position ->
            binding.vpNews.setCurrentItem(position, true)
            binding.appBar.setExpanded(false, true)
        }

        binding.vpNews.innerRecyclerView?.isNestedScrollingEnabled = false
        binding.vpNews.offscreenPageLimit = 20 // 被迫，要不然一堆高度问题
        binding.vpNews.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.rvTourSimplified.smoothScrollToPosition(position)
            }
        })

        initAnimation()

        //binding.srlPreview.setOnRefreshListener {
        //    viewModel.getHanimePreview(dateUtils.current.second)
        //}
    }

    @SuppressLint("SetTextI18n")
    override fun bindDataObservers() {
        lifecycleScope.launch {
            whenStarted {
                viewModel.previewFlow.collect { state ->
                    binding.nsvPreview.isGone = state !is WebsiteState.Success
                    binding.appBar.setExpanded(state is WebsiteState.Success, true)
                    when (state) {
                        is WebsiteState.Error -> {
                            //binding.srlPreview.finishRefresh()
                            supportActionBar?.title = "🥺\n${state.throwable.message}"
                        }

                        is WebsiteState.Loading -> {
                            //binding.srlPreview.autoRefresh()
                            viewModel.getHanimePreview(dateUtils.current.format(DateUtils.FORMATTED_FORMAT))
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
                                isEnabled = state.info.hasPrevious
                                text = dateUtils.prevDate.format(DateUtils.NORMAL_FORMAT)
                            }
                            binding.fabNext.apply {
                                isEnabled = state.info.hasNext
                                text = dateUtils.nextDate.format(DateUtils.NORMAL_FORMAT)
                            }
                            binding.cover.load(state.info.headerPicUrl) {
                                crossfade(true)
                            }
                            tourSimplifiedAdapter.submitList(state.info.latestHanime)
                            // 有時候 tour 無法順利加載出來，加點延遲反而就好了，哈哈
                            delay(100)
                            newsAdapter.submitList(state.info.previewInfo)
                        }
                    }
                }
            }
        }
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

    private fun initAnimation() {
        binding.appBar.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                when (state) {
                    State.EXPANDED -> {
                        binding.fabPrevious.animate().translationX(0F).setDuration(animDuration)
                            .setInterpolator(animInterpolator).start()
                        binding.fabNext.animate().translationX(0F).setDuration(animDuration)
                            .setInterpolator(animInterpolator).start()
                    }

                    State.INTERMEDIATE -> {
                        binding.fabPrevious.animate().translationX(-500F).setDuration(animDuration)
                            .setInterpolator(animInterpolator).start()
                        binding.fabNext.animate().translationX(500F).setDuration(animDuration)
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