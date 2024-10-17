package com.yenaly.han1meviewer.ui.fragment.search

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Checkable
import androidx.core.util.isNotEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.lxj.xpopupext.listener.TimePickerListener
import com.lxj.xpopupext.popup.TimePickerPopup
import com.yenaly.han1meviewer.FirebaseConstants
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.SEARCH_YEAR_RANGE_END
import com.yenaly.han1meviewer.SEARCH_YEAR_RANGE_START
import com.yenaly.han1meviewer.databinding.PopUpFragmentSearchOptionsBinding
import com.yenaly.han1meviewer.logic.model.SearchOption.Companion.get
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.ui.adapter.HSubscriptionAdapter
import com.yenaly.han1meviewer.ui.popup.HTimePickerPopup
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.han1meviewer.ui.viewmodel.SearchViewModel
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyBottomSheetDialogFragment
import com.yenaly.yenaly_libs.utils.mapToArray
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/08 008 17:04
 */
class SearchOptionsPopupFragment :
    YenalyBottomSheetDialogFragment<PopUpFragmentSearchOptionsBinding>() {

    companion object Tags {
        private const val POP_UP_BORDER_RADIUS = 36F
    }

    private val viewModel by activityViewModels<SearchViewModel>()
    val myListViewModel by activityViewModels<MyListViewModel>()

    /**
     * 是否用户真正使用了高级搜索里面的功能
     *
     * 用于 Firebase 统计
     */
    private var isUserUsed = false

    private var genres: Array<String>? = null
    private var sortOptions: Array<String>? = null
    private var durations: Array<String>? = null

    private val subscriptionAdapter by unsafeLazy {
        HSubscriptionAdapter()
    }

    // Popups

    private val timePickerPopup: TimePickerPopup
        get() {
            val date = Calendar.getInstance().also {
                viewModel.year?.let { year -> it.set(Calendar.YEAR, year) }
                viewModel.month?.let { month -> it.set(Calendar.MONTH, month - 1) }
            }
            val mode = when {
                viewModel.year != null && viewModel.month != null -> TimePickerPopup.Mode.YM
                viewModel.year != null -> TimePickerPopup.Mode.Y
                else -> TimePickerPopup.Mode.YM
            }
            val popup = HTimePickerPopup(requireContext())
                .apply popup@{
                    setMode(mode)
                    setYearRange(SEARCH_YEAR_RANGE_START, SEARCH_YEAR_RANGE_END)
                    setDefaultDate(date)
                    setTimePickerListener(object : TimePickerListener {
                        override fun onCancel() = Unit
                        override fun onTimeChanged(date: Date) = Unit

                        override fun onTimeConfirm(date: Date, view: View?) {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            when (this@popup.mode) {
                                TimePickerPopup.Mode.YM -> {
                                    viewModel.year = calendar.get(Calendar.YEAR)
                                    viewModel.month = calendar.get(Calendar.MONTH) + 1
                                }

                                else -> {
                                    viewModel.year = calendar.get(Calendar.YEAR)
                                    viewModel.month = null
                                }
                            }
                            isUserUsed = true
                            initOptionsChecked()
                        }
                    })
                }
            return XPopup.Builder(requireContext()).setOptionsCheckedCallback("release_dates")
                .borderRadius(POP_UP_BORDER_RADIUS)
                .isDarkTheme(true)
                .asCustom(popup) as TimePickerPopup
        }

    override fun getViewBinding(layoutInflater: LayoutInflater) =
        PopUpFragmentSearchOptionsBinding.inflate(layoutInflater)

    override fun initData(savedInstanceState: Bundle?, dialog: Dialog) {
        // #issue-199: 片长搜索官网取消了
        binding.duration.isAvailable = false
        // 简单的厂商搜索官网取消了
        binding.brand.isAvailable = false

        initOptionsChecked()
        initClick()
        initSubscription()
    }

    private fun initOptionsChecked() {
        binding.brand.isChecked = viewModel.brandMap.isNotEmpty()
        binding.sortOption.isChecked = viewModel.sort != null
        binding.duration.isChecked = viewModel.duration != null
        binding.tag.isChecked = viewModel.tagMap.isNotEmpty()
        binding.type.isChecked = viewModel.genre != null
        binding.releaseDate.isChecked = viewModel.year != null || viewModel.month != null
    }

    private fun initClick() {
        binding.type.apply {
            setOnClickListener {
                // typePopup.show()
                if (genres == null) {
                    genres = viewModel.genres.mapToArray { it.value }
                }
                requireContext().showAlertDialog(DialogInterface.OnDismissListener {
                    logAdvSearchEvent("genres")
                }) {
                    val index = viewModel.genres.indexOfFirst {
                        it.searchKey == viewModel.genre
                    }
                    setTitle(R.string.type)
                    setSingleChoiceItems(genres, index) { _, which ->
                        viewModel.genre = viewModel.genres.getOrNull(which)?.searchKey
                        isUserUsed = true
                        initOptionsChecked()
                    }
                    setPositiveButton(R.string.save, null)
                    setNeutralButton(R.string.reset) { _, _ ->
                        viewModel.genre = null
                        initOptionsChecked()
                    }
                }
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.genre = null
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        // deprecated
        binding.brand.apply {
            setOnClickListener {
                HMultiChoicesDialog(context, R.string.brand, hasSingleItem = true).apply {
                    addTagScope(null, viewModel.brands, spanCount = 2)
                }.apply {
                    loadSavedTags(viewModel.brandMap)
                    setOnSaveListener {
                        viewModel.brandMap = collectCheckedTags()
                        initOptionsChecked()
                        it.dismiss()
                    }
                    setOnResetListener {
                        clearAllChecks()
                        initOptionsChecked()
                    }
                }.show()
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.brandMap.clear()
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        binding.tag.apply {
            setOnClickListener {
                HMultiChoicesDialog(context, R.string.tag).apply {
                    addTagScope(
                        R.string.video_attr,
                        viewModel.tags[R.string.video_attr],
                        spanCount = 1
                    )
                    addTagScope(
                        R.string.relationship,
                        viewModel.tags[R.string.relationship],
                        spanCount = 2
                    )
                    addTagScope(
                        R.string.characteristics,
                        viewModel.tags[R.string.characteristics]
                    )
                    addTagScope(
                        R.string.appearance_and_figure,
                        viewModel.tags[R.string.appearance_and_figure]
                    )
                    addTagScope(
                        R.string.story_plot,
                        viewModel.tags[R.string.story_plot]
                    )
                    addTagScope(
                        R.string.sex_position,
                        viewModel.tags[R.string.sex_position]
                    )
                }.apply {
                    loadSavedTags(viewModel.tagMap)
                    setOnSaveListener {
                        viewModel.tagMap = collectCheckedTags()
                        initOptionsChecked()
                        isUserUsed = true
                        it.dismiss()
                    }
                    setOnResetListener {
                        clearAllChecks()
                        initOptionsChecked()
                    }
                    setOnDismissListener {
                        logAdvSearchEvent("tags")
                    }
                }.show()
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.tagMap.clear()
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        binding.sortOption.apply {
            setOnClickListener {
                // sortOptionPopup.show()
                if (sortOptions == null) {
                    sortOptions = viewModel.sortOptions.mapToArray { it.value }
                }
                requireContext().showAlertDialog(DialogInterface.OnDismissListener {
                    logAdvSearchEvent("sort_options")
                }) {
                    val index = viewModel.sortOptions.indexOfFirst {
                        it.searchKey == viewModel.sort
                    }
                    setTitle(R.string.sort_option)
                    setSingleChoiceItems(sortOptions, index) { _, which ->
                        viewModel.sort = viewModel.sortOptions.getOrNull(which)?.searchKey
                        isUserUsed = true
                        initOptionsChecked()
                    }
                    setPositiveButton(R.string.save, null)
                    setNeutralButton(R.string.reset) { _, _ ->
                        viewModel.sort = null
                        initOptionsChecked()
                    }
                }
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.sort = null
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        // deprecated
        binding.duration.apply {
            setOnClickListener {
                // durationPopup.show()
                if (durations == null) {
                    durations = viewModel.durations.mapToArray { it.value }
                }
                requireContext().showAlertDialog(DialogInterface.OnDismissListener {
                    initOptionsChecked()
                }) {
                    val index = viewModel.durations.indexOfFirst {
                        it.searchKey == viewModel.duration
                    }
                    setTitle(R.string.duration)
                    setSingleChoiceItems(durations, index) { _, which ->
                        viewModel.duration = viewModel.durations.getOrNull(which)?.searchKey
                        initOptionsChecked()
                    }
                    setPositiveButton(R.string.save, null)
                    setNeutralButton(R.string.reset) { _, _ ->
                        viewModel.duration = null
                        initOptionsChecked()
                    }
                }
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.duration = null
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        binding.releaseDate.apply {
            setOnClickListener {
                timePickerPopup.show()
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.year = null
                    viewModel.month = null
                    initOptionsChecked()
                }
                return@lc true
            }
        }
    }

    private fun initSubscription() {
        binding.rvSubscription.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSubscription.adapter = subscriptionAdapter
        if (isAlreadyLogin) {
            // 暂时只读取第一页
            lifecycleScope.launch {
                myListViewModel.subscription.subscriptionFlow.collect {
                    binding.llSubscription.isVisible = it.isNotEmpty()
                    subscriptionAdapter.submitList(it.map { subscription ->
                        subscription.copy(
                            isDeleteVisible = false,
                            isCheckBoxVisible = subscription.name == viewModel.subscriptionBrand
                        )
                    })
                }
            }
            lifecycleScope.launch {
                myListViewModel.subscription.deleteSubscriptionFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Success -> {
                            showShortToast(R.string.delete_success)
                            val position = state.info
                            val item = subscriptionAdapter.getItem(position) ?: return@collect
                            val activity = requireContext()
                            if (activity is SearchActivity && item.name == activity.searchText) {
                                activity.setSearchText(null)
                            }
                        }

                        is WebsiteState.Error -> {
                            showShortToast(R.string.delete_failed)
                            state.throwable.printStackTrace()
                        }

                        WebsiteState.Loading -> Unit
                    }
                }
            }
        }
    }

    private inline fun Checkable.showClearAllTagsDialog(crossinline action: () -> Unit) {
        if (isChecked) {
            requireContext().showAlertDialog {
                setTitle(R.string.alert)
                setMessage(R.string.alert_cancel_all_tags)
                setPositiveButton(R.string.confirm) { _, _ -> action.invoke() }
                setNegativeButton(R.string.cancel, null)
            }
        }
    }

    private fun XPopup.Builder.setOptionsCheckedCallback(type: String) = apply {
        setPopupCallback(object : SimpleCallback() {
            override fun beforeDismiss(popupView: BasePopupView?) {
                initOptionsChecked()
            }

            override fun onDismiss(popupView: BasePopupView?) {
                logAdvSearchEvent(type)
            }
        })
    }

    private fun logAdvSearchEvent(type: String, used: Boolean = isUserUsed) {
        Log.d("HFirebase", "logAdvSearchEvent: $type, $used")
        Firebase.analytics.logEvent(FirebaseConstants.ADV_SEARCH_OPT) {
            // 判断当前点击类型
            param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
            // 判断用户是否真正使用了高级搜索
            param("used", used.toString())
        }
        // 重置状态
        isUserUsed = false
    }
}