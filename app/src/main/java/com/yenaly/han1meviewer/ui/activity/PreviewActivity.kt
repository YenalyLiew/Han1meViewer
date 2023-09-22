package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.appbar.AppBarLayout
import com.yenaly.han1meviewer.DATE_CODE
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivityPreviewBinding
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.adapter.HanimePreviewNewsRvAdapter
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.PreviewViewModel
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.startActivity
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.AppBarLayoutStateChangeListener
import kotlinx.coroutines.launch
import java.util.*

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

    private val dateUtils by unsafeLazy { DateUtils() }

    private val tourAdapter by unsafeLazy { HanimeVideoRvAdapter() }
    private val newsAdapter by unsafeLazy { HanimePreviewNewsRvAdapter() }

    override fun setUiStyle() {
    }

    override fun initData(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeActionContentDescription(R.string.back)
        }

        binding.latestHanimeTour.title.setText(R.string.latest_hanime_tour)
        binding.latestHanimeTour.subTitle.setText(R.string.this_month)
        binding.latestHanimeNews.title.setText(R.string.latest_hanime_news)
        binding.latestHanimeNews.subTitle.setText(R.string.introduction)

        binding.fabPrevious.setOnClickListener {
            viewModel.getHanimePreview(dateUtils.toPreviousDate().second)
        }
        binding.fabNext.setOnClickListener {
            viewModel.getHanimePreview(dateUtils.toNextDate().second)
        }

        binding.appBar.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
            private val fabList = arrayOf(binding.fabPrevious, binding.fabNext)
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                if (state == State.COLLAPSED) {
                    fabList.forEach {
                        it.animate()
                            .setDuration(animDuration)
                            .setInterpolator(animInterpolator)
                            .alpha(0F)
                            .withEndAction { it.isInvisible = true }
                            .start()
                    }
                } else {
                    fabList.forEach {
                        it.animate()
                            .setDuration(animDuration)
                            .setInterpolator(animInterpolator)
                            .alpha(1F)
                            .withStartAction { it.isInvisible = false }
                            .start()
                    }
                }
            }
        })

        binding.latestHanimeTour.rv.apply {
            layoutManager = object : LinearLayoutManager(this@PreviewActivity) {

                init {
                    orientation = HORIZONTAL
                    reverseLayout = false
                }

                override fun scrollVerticallyBy(
                    dy: Int,
                    recycler: RecyclerView.Recycler?,
                    state: RecyclerView.State?,
                ): Int {
                    if (!binding.latestHanimeNews.rv.isInTouchMode) {
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
            adapter = tourAdapter
        }
        binding.latestHanimeNews.rv.apply {
            layoutManager = LinearLayoutManager(this@PreviewActivity)
            adapter = newsAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@PreviewActivity, DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(getDrawable(R.drawable.line_divider)!!)
                }
            )
        }
        tourAdapter.setOnItemClickListener { _, _, position ->
            val y = binding.latestHanimeNews.rv.getChildAt(position).y
            binding.nsvPreview.fling(0)
            binding.appBar.setExpanded(false, true)
            binding.nsvPreview.smoothScrollTo(0, y.toInt())
        }

        binding.srlPreview.setOnRefreshListener {
            viewModel.getHanimePreview(dateUtils.current.second)
        }
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
                            binding.srlPreview.finishRefresh()
                            supportActionBar?.title = "🥺\n${state.throwable.message}"
                        }

                        is WebsiteState.Loading -> {
                            binding.srlPreview.autoRefresh()
                            binding.fabPrevious.isEnabled = false
                            binding.fabNext.isEnabled = false
                        }

                        is WebsiteState.Success -> {
                            binding.srlPreview.finishRefresh()
                            supportActionBar?.title =
                                getString(
                                    R.string.latest_hanime_list_monthly,
                                    dateUtils.current.first
                                )
                            binding.fabPrevious.apply {
                                isEnabled = state.info.hasPrevious
                                text = dateUtils.getPreviousDate().first
                            }
                            binding.fabNext.apply {
                                isEnabled = state.info.hasNext
                                text = dateUtils.getNextDate().first
                            }
                            binding.cover.load(state.info.headerPicUrl) {
                                crossfade(true)
                            }
                            tourAdapter.setList(state.info.latestHanime)
                            newsAdapter.setList(state.info.previewInfo)
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
                    "date" to dateUtils.current.first,
                    DATE_CODE to dateUtils.current.second
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 单纯给这个用的DateUtils
     */
    private inner class DateUtils {

        // 2022/2 to 202202 后者传参有用
        var current: Pair</* 普通日期 */String, /* 格式化后的日期 */String> = getCurrentDate()
            private set

        // get 和 to 的区别：get不覆盖current，to覆盖

        fun getCurrentDate(): Pair<String, String> {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            return "${year}/${month}" to (if (month < 10) "${year}0${month}" else "$year$month")
        }

        fun getPreviousDate(): Pair<String, String> {
            val calendar = Calendar.getInstance()
            val year = current.second.substring(0, 4).toInt()
            val month = current.second.substring(4, 6).toInt()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.add(Calendar.MONTH, -1)
            val previousYear = calendar.get(Calendar.YEAR)
            val previousMonth = calendar.get(Calendar.MONTH) + 1
            return "${previousYear}/${previousMonth}" to (if (previousMonth < 10) {
                "${previousYear}0${previousMonth}"
            } else "$previousYear$previousMonth")
        }

        fun getNextDate(): Pair<String, String> {
            val calendar = Calendar.getInstance()
            val year = current.second.substring(0, 4).toInt()
            val month = current.second.substring(4, 6).toInt()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.add(Calendar.MONTH, 1)
            val nextYear = calendar.get(Calendar.YEAR)
            val nextMonth = calendar.get(Calendar.MONTH) + 1
            return "${nextYear}/${nextMonth}" to (if (nextMonth < 10) {
                "${nextYear}0${nextMonth}"
            } else "$nextYear$nextMonth")
        }

        fun toPreviousDate(): Pair<String, String> {
            current = getPreviousDate()
            return current
        }

        fun toNextDate(): Pair<String, String> {
            current = getNextDate()
            return current
        }
    }
}