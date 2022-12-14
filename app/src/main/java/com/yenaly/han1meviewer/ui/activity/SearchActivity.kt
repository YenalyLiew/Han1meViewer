package com.yenaly.han1meviewer.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.impl.CenterListPopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.lxj.xpopupext.listener.TimePickerListener
import com.lxj.xpopupext.popup.TimePickerPopup
import com.mancj.materialsearchbar.MaterialSearchBar
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.databinding.ActivitySearchBinding
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.model.HanimeInfoModel
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.ui.adapter.FixedGridLayoutManager
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.adapter.SearchHistoryRvAdapter
import com.yenaly.han1meviewer.ui.popup.HanimeSearchTagCenterPopup
import com.yenaly.han1meviewer.ui.viewmodel.SearchViewModel
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.intentExtra
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.hideIme
import kotlinx.coroutines.launch
import java.util.*

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/13 013 22:29
 */
class SearchActivity : YenalyActivity<ActivitySearchBinding, SearchViewModel>() {

    // type ?????? genre
    private var typePosition: Int? = null
    private val typeArray =
        arrayOf("??????", "??????", "?????????", "3D??????", "????????????", "Cosplay")

    private val videoAttrTagArray =
        arrayOf("??????", "AI??????", "1080p", "60FPS")
    private val relationshipTagArray =
        arrayOf("??????", "???", "???", "???", "??????", "??????", "??????", "????????????")
    private val characterSettingTagArray =
        arrayOf(
            "JK",
            "??????",
            "??????",
            "??????",
            "??????",
            "??????",
            "????????????",
            "OL",
            "?????????",
            "??????",
            "??????",
            "??????",
            "??????",
            "?????????",
            "??????",
            "?????????",
            "????????????",
            "?????????",
            "??????",
            "?????????",
            "??????",
            "??????",
            "??????",
            "????????????",
            "??????",
            "??????",
            "??????",
            "??????",
            "??????"
        )
    private val appearanceTagArray =
        arrayOf(
            "??????",
            "??????",
            "?????????",
            "??????",
            "??????",
            "?????????",
            "?????????",
            "??????",
            "?????????",
            "?????????",
            "??????",
            "??????",
            "?????????",
            "?????????",
            "??????",
            "?????????",
            "??????",
            "?????????",
            "??????",
            "?????????",
            "??????",
            "??????",
            "?????????",
            "??????",
            "?????????",
            "????????????",
            "?????????",
            "?????????",
            "??????"
        )
    private val storyPlotTagArray =
        arrayOf(
            "??????",
            "????????????",
            "??????",
            "?????????",
            "????????????",
            "NTR",
            "????????????",
            "??????",
            "??????",
            "?????????",
            "????????????",
            "??????",
            "BDSM",
            "??????",
            "??????",
            "??????",
            "??????",
            "????????????",
            "?????????",
            "??????",
            "??????",
            "?????????",
            "?????????",
            "?????????",
            "?????????",
            "??????",
            "??????",
            "??????",
            "?????????",
            "??????",
            "??????",
            "?????????",
            "??????",
            "????????????"
        )
    private val sexPositionTagArray =
        arrayOf(
            "??????",
            "??????",
            "??????",
            "??????",
            "??????",
            "??????",
            "3P",
            "??????",
            "??????",
            "??????",
            "??????",
            "?????????",
            "??????",
            "69",
            "??????",
            "??????",
            "??????",
            "?????????",
            "??????",
            "??????",
            "????????????",
            "??????",
            "??????",
            "??????",
            "??????",
            "????????????",
            "??????",
            "?????????",
            "?????????",
            "??????",
            "?????????"
        )

    private var sortOptionPosition: Int? = null
    private val sortOptionArray =
        arrayOf("????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????")

    private val brandArray =
        arrayOf(
            "????????????????????????",
            "????????????????????????",
            "???????????????????????????",
            "????????????????????????",
            "Queen Bee",
            "PoRO",
            "?????????????????????",
            "???????????????",
            "???????????????",
            "GOLD BEAR",
            "ZIZ",
            "EDGE",
            "Collaboration Works",
            "BOOTLEG",
            "BOMB!CUTE!BOMB!",
            "nur",
            "?????????????????????",
            "??????",
            "??????",
            "Princess Sugar",
            "???????????????",
            "WHITE BEAR",
            "AniMan",
            "chippai",
            "????????????????????????",
            "erozuki",
            "??????????????????????????????",
            "spermation",
            "Milky",
            "King Bee",
            "PashminaA",
            "???????????????????????????",
            "Hills",
            "????????????",
            "?????????????????????",
            "????????????",
            "37???",
            "schoolzone",
            "GREEN BUNNY",
            "?????????",
            "L.",
            "PIXY",
            "?????????????????????",
            "ANIMAC",
            "Celeb",
            "MOON ROCK",
            "Dream",
            "?????????",
            "??????????????????",
            "????????????",
            "????????????????????????",
            "????????????????????????",
            "?????????????????????",
            "???????????????????????????",
            "UNION-CHO",
            "TOHO",
            "??????????????????",
            "2??????????????????",
            "?????????????????????????????????",
            "??????????????????",
            "?????????????????????????????????",
            "???????????????????????????",
            "?????????????????????????????????",
            "?????????",
            "?????????????????????",
            "??????????????????"
        )

    private var durationPosition: Int? = null
    private val durationMap =
        linkedMapOf(
            "??????" to null,
            "?????????4????????????" to "??????",
            "????????????4???20?????????" to "?????????",
            "?????????20???????????????" to "??????"
        )

    private val searchAdapter by unsafeLazy { HanimeVideoRvAdapter() }
    private val fromVideoTag by intentExtra<String>(FROM_VIDEO_TAG)

    // ??????????????????show??????????????????builder????????????
    @UsingCautiously
    private lateinit var brandPopup: BasePopupView

    @UsingCautiously
    private lateinit var tagPopup: BasePopupView

    // ???????????????XPopup???asCustom???????????????????????????show
    @UsingCautiously
    private lateinit var timePickerPopup: TimePickerPopup

    override fun setUiStyle() {
        // SystemStatusUtil.fullScreen(window, true)
    }

    /**
     * ???????????????
     */
    override fun initData(savedInstanceState: Bundle?) {
        initSearchBar()

        initChip()

        binding.searchRv.apply {
            layoutManager = FixedGridLayoutManager(
                this@SearchActivity,
                if (searchAdapter.getItemViewType(0) == HanimeInfoModel.NORMAL)
                    VIDEO_IN_ONE_LINE else SIMPLIFIED_VIDEO_IN_ONE_LINE
            )
            adapter = searchAdapter
            addOnScrollListener(object : OnScrollListener() {
                val searchBar = binding.searchBar.searchBar
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                        if (binding.searchBar.groupTag.isVisible) {
                            binding.searchBar.groupTag.fadeGone()
                        }
                        if (searchBar.isSuggestionsVisible) {
                            searchBar.hideSuggestionsList()
                        }
                        if (searchBar.isSearchOpened) {
                            searchBar.searchEditText.hideIme(window)
                        }
                    }
                }
            })
        }
        binding.searchSrl.apply {
            setOnLoadMoreListener {
                getHanimeSearchResult()
            }
            setOnRefreshListener {
                // will enter here firstly. cuz the flow's def value is Loading.
                getNewHanimeSearchResult()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun liveDataObserve() {
        lifecycleScope.launch {
            whenStarted {
                viewModel.searchFlow.collect { state ->
                    when (state) {
                        is PageLoadingState.Loading -> {
                            // ????????????list?????????????????????empty view?????????????????????
                            searchAdapter.removeEmptyView()
                            if (searchAdapter.data.isEmpty()) binding.searchSrl.autoRefresh()
                        }
                        is PageLoadingState.Success -> {
                            viewModel.page++
                            if (binding.searchSrl.isRefreshing) binding.searchSrl.finishRefresh()
                            binding.searchSrl.finishLoadMore(true)
                            searchAdapter.addData(state.info)
                        }
                        is PageLoadingState.NoMoreData -> {
                            binding.searchSrl.finishLoadMoreWithNoMoreData()
                            if (searchAdapter.data.isEmpty()) searchAdapter.setEmptyView(R.layout.layout_empty_view)
                        }
                        is PageLoadingState.Error -> {
                            if (binding.searchSrl.isRefreshing) binding.searchSrl.finishRefresh()
                            binding.searchSrl.finishLoadMore(false)
                            // set error view
                            val errView = LayoutInflater.from(this@SearchActivity).inflate(
                                R.layout.layout_empty_view,
                                searchAdapter.recyclerViewOrNull,
                                false
                            )
                            errView.findViewById<TextView>(R.id.tv_empty).text =
                                "????\n${state.throwable.message}"
                            searchAdapter.setEmptyView(errView)
                        }
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.searchRv.layoutManager = GridLayoutManager(
            this@SearchActivity,
            if (searchAdapter.getItemViewType(0) == HanimeInfoModel.NORMAL)
                VIDEO_IN_ONE_LINE else SIMPLIFIED_VIDEO_IN_ONE_LINE
        )
        // re-initial popup to resize height
        initBrandPopup()
        initTagPopup()
    }

    override fun onBackPressed() {
        if (binding.searchBar.groupTag.isVisible) {
            binding.searchBar.groupTag.fadeGone()
            return
        }
        super.onBackPressed()
    }

    private fun getHanimeSearchResult() {
        viewModel.getHanimeSearchResult(
            viewModel.page,
            viewModel.query, viewModel.genre, viewModel.sort, viewModel.broad,
            viewModel.year, viewModel.month, viewModel.duration,
            viewModel.tagSet, viewModel.brandSet
        )
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    private fun getNewHanimeSearchResult() {
        searchAdapter.data.clear()
        viewModel.page = 1
        getHanimeSearchResult()
    }

    private fun initSearchBar() {

        // ??????????????????????????????????????????PlaceHolder???????????????????????????????????????PlaceHolder????????????????????????
        binding.searchBar.searchBar.setTextListener { text ->
            text?.let {
                binding.searchBar.searchBar.setPlaceHolder(it.ifBlank {
                    getString(R.string.search_placeholder)
                })
            }
        }

        // ?????????????????????????????????????????????
        binding.searchBar.searchBar.searchEditText.addTextChangedListener { text ->
            text?.let {
                if (text.isBlank() && !binding.searchBar.searchBar.isSuggestionsVisible && binding.searchBar.searchBar.isSearchOpened) {
                    binding.searchBar.searchBar.showSuggestionsList()
                }
            }
        }

        fromVideoTag?.let {
            binding.searchBar.searchBar.appendTextWithListener(it)
            viewModel.query = it
        }

        val searchHistoryRvAdapter = SearchHistoryRvAdapter(LayoutInflater.from(this)).apply {
            setListener(object : SearchHistoryRvAdapter.OnItemViewClickListener {
                override fun onItemClickListener(suggestion: String, v: View?) {
                    binding.searchBar.searchBar.appendText(suggestion)
                }

                override fun onItemDeleteListener(suggestion: String, v: View?) {
                    // ???????????????????????????????????????????????????????????????
                    if (suggestions.size == 1) {
                        binding.searchBar.searchBar.hideSuggestionsList()
                    }
                    viewModel.deleteSearchHistoryByKeyword(suggestion)
                    suggestions.remove(suggestion)
                    binding.searchBar.searchBar.updateLastSuggestions(suggestions)
                }
            })
        }
        binding.searchBar.searchBar.setCustomSuggestionAdapter(searchHistoryRvAdapter)
        binding.searchBar.btnBack.setOnClickListener { finish() }
        binding.searchBar.btnTag.setOnClickListener {
            if (binding.searchBar.searchBar.isSuggestionsVisible) {
                binding.searchBar.searchBar.hideSuggestionsList()
            }
            if (binding.searchBar.groupTag.isGone) {
                binding.searchBar.groupTag.fadeShow()
            } else binding.searchBar.groupTag.fadeGone()
        }

        lifecycleScope.launch {
            whenStarted {
                viewModel.loadAllSearchHistories().collect { entities ->
                    binding.searchBar.searchBar.lastSuggestions = entities.map { it.query }
                }
            }
        }

        binding.searchBar.searchBar.setOnSearchActionListener(
            object : MaterialSearchBar.OnSearchActionListener {
                override fun onSearchStateChanged(enabled: Boolean) {
                    if (enabled) {
                        if (binding.searchBar.groupTag.isVisible) {
                            binding.searchBar.groupTag.fadeGone()
                        }
                    }
                }

                override fun onSearchConfirmed(text: CharSequence?) {
                    binding.searchBar.searchBar.searchEditText.hideIme(window)
                    if (binding.searchBar.groupTag.isVisible) {
                        binding.searchBar.groupTag.fadeGone()
                    }
                    text?.toString()?.let {
                        viewModel.query = it.ifBlank { null }?.also { query ->
                            viewModel.insertSearchHistory(SearchHistoryEntity(query))
                        }
                        binding.searchBar.searchBar.setPlaceHolder(it.ifBlank {
                            getString(R.string.search_placeholder)
                        })
                    }
                    // getNewHanimeSearchResult() ???????????????????????????????????????????????????
                    binding.searchSrl.autoRefresh()
                }

                override fun onButtonClicked(buttonCode: Int) = Unit
            }
        )
    }

    private fun initChip() {

        initBrandPopup()
        initTagPopup()
        initTimePickerPopup()

        binding.searchBar.type.apply {
            setOnClickListener {
                binding.searchBar.type.isChecked = true
                makeCenterListPopup(
                    getString(R.string.type_able_reset), typeArray, typePosition,
                    beforeDismiss = { binding.searchBar.type.isChecked = typePosition != null }
                ) { position, text ->
                    viewModel.genre = if (typePosition != position) text else null
                    typePosition = if (typePosition != position) position else null
                }.show()
            }
            setOnLongClickListener {
                showClearAllTagsDialog {
                    typePosition = null
                    viewModel.genre = null
                    binding.searchBar.type.isChecked = false
                }
                return@setOnLongClickListener true
            }
        }
        binding.searchBar.sortOption.apply {
            setOnClickListener {
                binding.searchBar.sortOption.isChecked = true
                makeCenterListPopup(
                    getString(R.string.sort_option_able_reset), sortOptionArray, sortOptionPosition,
                    beforeDismiss = {
                        binding.searchBar.sortOption.isChecked = sortOptionPosition != null
                    }
                ) { position, text ->
                    viewModel.sort = if (sortOptionPosition != position) text else null
                    sortOptionPosition = if (sortOptionPosition != position) position else null
                }.show()
            }
            setOnLongClickListener {
                showClearAllTagsDialog {
                    sortOptionPosition = null
                    viewModel.sort = null
                    binding.searchBar.sortOption.isChecked = false
                }
                return@setOnLongClickListener true
            }
        }
        binding.searchBar.brand.apply {
            setOnClickListener {
                binding.searchBar.brand.isChecked = true
                brandPopup.show()
            }
            setOnLongClickListener {
                showClearAllTagsDialog {
                    viewModel.brandSet.clear()
                    binding.searchBar.brand.isChecked = false
                }
                return@setOnLongClickListener true
            }
        }
        binding.searchBar.tag.apply {
            setOnClickListener {
                binding.searchBar.tag.isChecked = true
                tagPopup.show()
            }
            setOnLongClickListener {
                showClearAllTagsDialog {
                    viewModel.tagSet.clear()
                    binding.searchBar.tag.isChecked = false
                }
                return@setOnLongClickListener true
            }
        }
        binding.searchBar.releaseDate.apply {
            setOnClickListener {
                binding.searchBar.releaseDate.isChecked = true
                if (viewModel.year != null && viewModel.month != null) {
                    val calendar = Calendar.getInstance()
                    calendar.set(viewModel.year!!, viewModel.month!!, 0)
                    timePickerPopup.setDefaultDate(calendar)
                }
                XPopup.Builder(this@SearchActivity)
                    .setPopupCallback(object : SimpleCallback() {
                        override fun beforeDismiss(popupView: BasePopupView?) {
                            binding.searchBar.releaseDate.isChecked =
                                viewModel.year != null || viewModel.month != null
                        }
                    })
                    .isDarkTheme(true).asCustom(timePickerPopup).show()
            }
            setOnLongClickListener {
                showClearAllTagsDialog {
                    binding.searchBar.releaseDate.isChecked = false
                    viewModel.year = null
                    viewModel.month = null
                }
                return@setOnLongClickListener true
            }
        }
        binding.searchBar.duration.apply {
            setOnClickListener {
                binding.searchBar.duration.isChecked = true
                makeCenterListPopup(
                    getString(R.string.duration_able_reset),
                    durationMap.keys.toTypedArray(),
                    durationPosition,
                    beforeDismiss = {
                        binding.searchBar.duration.isChecked = durationPosition != null
                    }
                ) { position, text ->
                    viewModel.duration =
                        if (durationPosition != position) durationMap[text] else null
                    durationPosition = if (durationPosition != position) position else null
                    getNewHanimeSearchResult()
                }.show()
            }
            setOnLongClickListener {
                showClearAllTagsDialog {
                    durationPosition = null
                    viewModel.duration = null
                    binding.searchBar.duration.isChecked = false
                }
                return@setOnLongClickListener true
            }
        }
    }

    // base
    private fun makeCenterListPopup(
        title: String,
        list: Array<String>,
        position: Int?,
        beforeDismiss: (BasePopupView) -> Unit,
        action: (position: Int, text: String) -> Unit
    ): CenterListPopupView {
        val simpleCallback = object : SimpleCallback() {
            override fun beforeDismiss(popupView: BasePopupView) {
                beforeDismiss.invoke(popupView)
            }
        }
        return if (position != null) {
            XPopup.Builder(this).setPopupCallback(simpleCallback)
                .isDarkTheme(true)
                .asCenterList(title, list, null, position, action)
        } else {
            XPopup.Builder(this).setPopupCallback(simpleCallback)
                .isDarkTheme(true)
                .asCenterList(title, list, action)
        }
    }

    // base
    private fun makeCenterTagPopup(
        beforeShow: (BasePopupView, SwitchMaterial, List<Chip>) -> Unit,
        beforeDismiss: (BasePopupView) -> Unit,
        action: HanimeSearchTagCenterPopup.() -> Unit
    ): BasePopupView {
        val popup = HanimeSearchTagCenterPopup(this)
        popup.action()
        return XPopup.Builder(this).setPopupCallback(object : SimpleCallback() {
            override fun beforeDismiss(popupView: BasePopupView) {
                beforeDismiss.invoke(popupView)
            }

            override fun beforeShow(popupView: BasePopupView) {
                beforeShow.invoke(popupView, popup.pairWidely, popup.chipList)
            }
        })
            .asCustom(popup)
    }

    private fun initBrandPopup() {
        brandPopup = makeCenterTagPopup(
            beforeShow = { _, _, list ->
                for (chip in list) {
                    chip.isChecked = chip.text in viewModel.brandSet
                }
            },
            beforeDismiss = { binding.searchBar.brand.isChecked = viewModel.brandSet.isNotEmpty() }
        ) {
            setTitle(getString(R.string.brand))
            showPairWidelyLayout(false)
            addTagsScope {
                addTagGroup(null, brandArray, null)
            }
            setOnResetClickListener {
                chipList.forEach { tag ->
                    tag.isChecked = false
                }
            }
            setOnSaveClickListener {
                viewModel.brandSet.clear()
                chipList.forEach { tag ->
                    if (tag.isChecked) viewModel.brandSet += tag.text.toString()
                }
                if (chipList.isEmpty()) binding.searchBar.brand.isChecked = false
                dismiss()
            }
        }
    }

    private fun initTagPopup() {
        tagPopup = makeCenterTagPopup(
            beforeShow = { _, switch, list ->
                switch.isChecked = viewModel.broad != null
                for (chip in list) {
                    chip.isChecked = chip.text in viewModel.tagSet
                }
            },
            beforeDismiss = { binding.searchBar.tag.isChecked = viewModel.tagSet.isNotEmpty() }
        ) {
            // kotlin-dsl style
            setTitle(getString(R.string.tag))
            showPairWidelyLayout(true)

            var broad: String? = null
            setOnPairWidelySwitchCheckedListener { _, isChecked ->
                broad = if (isChecked) "on" else null
            }

            addTagsScope {
                addTagGroup(getString(R.string.video_attr), videoAttrTagArray, null)
                addTagGroup(getString(R.string.relationship), relationshipTagArray, null)
                addTagGroup(getString(R.string.character_setting), characterSettingTagArray, null)
                addTagGroup(getString(R.string.appearance_and_figure), appearanceTagArray, null)
                addTagGroup(getString(R.string.story_plot), storyPlotTagArray, null)
                addTagGroup(getString(R.string.sex_position), sexPositionTagArray, null)
            }
            setOnResetClickListener {
                pairWidely.isChecked = false
                chipList.forEach { tag ->
                    tag.isChecked = false
                }
            }
            setOnSaveClickListener {
                viewModel.broad = broad
                viewModel.tagSet.clear()
                chipList.forEach { tag ->
                    if (tag.isChecked) viewModel.tagSet += tag.text.toString()
                }
                if (chipList.isEmpty()) binding.searchBar.tag.isChecked = false
                dismiss()
            }
        }
    }


    private fun initTimePickerPopup() {
        timePickerPopup = TimePickerPopup(this)
            .setMode(TimePickerPopup.Mode.YM)
            .setYearRange(SEARCH_YEAR_RANGE_START, SEARCH_YEAR_RANGE_END)
            .setTimePickerListener(object : TimePickerListener {
                override fun onTimeChanged(date: Date) = Unit

                override fun onTimeConfirm(date: Date, view: View) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    viewModel.year = calendar.get(Calendar.YEAR)
                    viewModel.month = calendar.get(Calendar.MONTH) + 1
                }
            })
    }

    private fun Chip.showClearAllTagsDialog(action: () -> Unit) {
        if (isChecked) {
            MaterialAlertDialogBuilder(this.context)
                .setTitle(R.string.alert)
                .setMessage(R.string.alert_cancel_all_tags)
                .setPositiveButton(R.string.confirm) { _, _ -> action.invoke() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private val defaultInterpolator = AccelerateDecelerateInterpolator()

    private fun View.fadeShow() {
        alpha = 0f
        isVisible = true
        animate().alpha(1f).setDuration(500).setInterpolator(defaultInterpolator)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isVisible = true
                }
            }).start()
    }

    private fun View.fadeGone() {
        animate().alpha(0f).setDuration(500).setInterpolator(defaultInterpolator)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isVisible = false
                }
            }).start()
    }
}