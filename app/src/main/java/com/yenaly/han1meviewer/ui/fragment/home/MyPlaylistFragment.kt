package com.yenaly.han1meviewer.ui.fragment.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VideoCoverSize
import com.yenaly.han1meviewer.databinding.FragmentPlaylistBinding
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeMyListVideoAdapter
import com.yenaly.han1meviewer.ui.adapter.PlaylistRvAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.fragment.LoginNeededFragmentMixin
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.clickTrigger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:43
 */
class MyPlaylistFragment : YenalyFragment<FragmentPlaylistBinding>(),
    IToolbarFragment<MainActivity>, LoginNeededFragmentMixin, StateLayoutMixin {

    val viewModel by activityViewModels<MyListViewModel>()

    private var page: Int
        set(value) {
            viewModel.playlist.playlistPage = value
        }
        get() = viewModel.playlist.playlistPage

    var listCode: String?
        set(value) {
            viewModel.playlist.playlistCode = value
            binding.playlistHeader.isVisible = value != null
        }
        get() = viewModel.playlist.playlistCode

    var listTitle: String?
        set(value) {
            viewModel.playlist.playlistTitle = value
            binding.playlistHeader.title = value
        }
        get() = viewModel.playlist.playlistTitle

    private var listDesc: String?
        set(value) {
            viewModel.playlist.playlistDesc = value
            binding.playlistHeader.description = value
        }
        get() = viewModel.playlist.playlistDesc

    private val adapter by unsafeLazy { HanimeMyListVideoAdapter() }
    private val playlistsAdapter by unsafeLazy { PlaylistRvAdapter(this) }

    /**
     * 用於判斷是否需要 setExpanded，防止重複喚出 AppBar
     */
    private var isAfterRefreshing = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlaylistBinding {
        return FragmentPlaylistBinding.inflate(inflater, container, false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        checkLogin()
        (activity as MainActivity).setupToolbar()
        binding.statePlaylist.init {
            loadingLayout = R.layout.layout_empty_view
            onLoading {
                findViewById<TextView>(R.id.tv_empty).setText(R.string.loading)
            }
        }
        binding.statePageList.init {
            loadingLayout = R.layout.layout_empty_view
            onLoading {
                findViewById<TextView>(R.id.tv_empty).setText(R.string.slide_to_choose_list)
            }
        }

        binding.rvPlaylist.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playlistsAdapter
        }

        binding.rvPageList.apply {
            layoutManager = GridLayoutManager(context, VideoCoverSize.Simplified.videoInOneLine)
            adapter = this@MyPlaylistFragment.adapter
        }

        initPlaylistHeader()

        viewModel.playlist.getPlaylists()

        adapter.setOnItemLongClickListener { _, _, position ->
            val item = adapter.getItem(position) ?: return@setOnItemLongClickListener true
            requireContext().showAlertDialog {
                setTitle(R.string.delete_playlist)
                setMessage(getString(R.string.sure_to_delete_s, item.title))
                setPositiveButton(R.string.confirm) { _, _ ->
                    listCode?.let { listCode ->
                        viewModel.playlist.deleteFromPlaylist(listCode, item.videoCode, position)
                    }
                }
                setNegativeButton(R.string.cancel, null)
            }
            return@setOnItemLongClickListener true
        }

        binding.btnRefreshPlaylists.clickTrigger(viewLifecycleOwner.lifecycle) {
            viewModel.playlist.getPlaylists()
        }
        binding.btnNewPlaylist.setOnClickListener {
            requireContext().showAlertDialog {
                setTitle(R.string.create_new_playlist)
                val etView = View.inflate(context, R.layout.dialog_playlist_modify_edit_text, null)
                val etTitle = etView.findViewById<EditText>(R.id.et_title)
                val etDesc = etView.findViewById<EditText>(R.id.et_desc)
                setView(etView)
                setPositiveButton(R.string.confirm) { _, _ ->
                    viewModel.playlist.createPlaylist(
                        etTitle.text.toString(),
                        etDesc.text.toString()
                    )
                }
                setNegativeButton(R.string.cancel, null)

            }
        }

        binding.srlPageList.apply {
            setOnLoadMoreListener {
                getPlaylistItems()
            }
            setOnRefreshListener {
                getNewPlaylistItems()
            }
            setDisableContentWhenRefresh(true)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.playlistHeader.isVisible = listCode != null
        binding.playlistHeader.title = listTitle
        binding.playlistHeader.description = listDesc
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlist.playlistStateFlow.collect { state ->
                    val isExist =
                        state is PageLoadingState.Success || state is PageLoadingState.NoMoreData
                    if (!isAfterRefreshing) {
                        binding.appBar.setExpanded(isExist, true)
                        binding.playlistHeader.isVisible =
                            isExist || binding.playlistHeader.isVisible // 只有在刚开始的时候是不可见的
                    }
                    when (state) {
                        is PageLoadingState.Error -> {
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(false)
                            // set error view
                            binding.statePageList.showError(state.throwable)
                        }

                        is PageLoadingState.Loading -> {
                            adapter.stateView = null
                            if (listCode == null) {
                                binding.statePageList.showLoading()
                            } else if (viewModel.playlist.playlistFlow.value.isEmpty()) {
                                binding.srlPageList.autoRefresh()
                            }
                        }

                        is PageLoadingState.NoMoreData -> {
                            binding.srlPageList.finishLoadMoreWithNoMoreData()
                            binding.srlPageList.finishRefresh()
                            if (viewModel.playlist.playlistFlow.value.isEmpty()) {
                                adapter.notifyDataSetChanged() // 這裡要用notifyDataSetChanged()，不然不會出現空白頁，而且crash
                                binding.statePageList.showEmpty()
                            }
                        }

                        is PageLoadingState.Success -> {
                            page++
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(true)
                            if (!isAfterRefreshing) {
                                binding.cover.load(state.info.hanimeInfo.firstOrNull()?.coverUrl) {
                                    crossfade(true)
                                }
                            }
                            isAfterRefreshing = true
                            listDesc = state.info.desc
                            binding.statePageList.showContent()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlist.playlistFlow.collectLatest { list ->
                    if (listCode != null) adapter.submitList(list)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlist.playlistsFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Success -> {
                            playlistsAdapter.submitList(state.info.playlists)
                            if (state.info.playlists.isEmpty()) {
                                binding.statePlaylist.showEmpty()
                            } else {
                                binding.statePlaylist.showContent()
                            }
                        }

                        is WebsiteState.Error -> {
                            binding.statePlaylist.showError()
                        }

                        is WebsiteState.Loading -> {
                            binding.statePlaylist.showLoading()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlist.deleteFromPlaylistFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.delete_failed)
                    }

                    is WebsiteState.Loading -> {
                    }

                    is WebsiteState.Success -> {
                        showShortToast(R.string.delete_success)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlist.modifyPlaylistFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.modify_failed)
                    }

                    is WebsiteState.Loading -> {
                    }

                    is WebsiteState.Success -> {
                        showShortToast(R.string.modify_success)
                        if (state.info.isDeleted) {
                            listCode = null
                            listTitle = null
                            listDesc = null
                            binding.appBar.setExpanded(false, true)
                            binding.statePageList.showLoading()
                        } else {
                            listTitle = state.info.title
                            listDesc = state.info.desc
                        }
                        viewModel.playlist.getPlaylists()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlist.createPlaylistFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.add_failed)
                    }

                    is WebsiteState.Loading -> Unit
                    is WebsiteState.Success -> {
                        showShortToast(R.string.add_success)
                        viewModel.playlist.getPlaylists()
                    }
                }
            }
        }
    }

    private fun getPlaylistItems() {
        val listCode = listCode
        if (listCode != null) {
            viewModel.playlist.getPlaylistItems(page, listCode = listCode)
        } else {
            binding.statePageList.showLoading()
        }
    }

    fun getNewPlaylistItems() {
        page = 1
        isAfterRefreshing = false
        viewModel.playlist.clearMyListItems()
        getPlaylistItems()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.rvPageList.layoutManager =
            GridLayoutManager(context, VideoCoverSize.Simplified.videoInOneLine)
    }

    override fun MainActivity.setupToolbar() {
        val toolbar = this@MyPlaylistFragment.binding.toolbar
        val dlPlaylist = this@MyPlaylistFragment.binding.dlPlaylist
        setSupportActionBar(toolbar)
        supportActionBar!!.setSubtitle(R.string.play_list)
        this@MyPlaylistFragment.addMenu(
            R.menu.menu_playlist_toolbar, viewLifecycleOwner
        ) { menuItem ->
            when (menuItem.itemId) {
                R.id.tb_open_drawer -> {
                    dlPlaylist.openDrawer(GravityCompat.END)
                    return@addMenu true
                }
            }
            return@addMenu false
        }

        toolbar.setupWithMainNavController()
    }

    private fun initPlaylistHeader() {
        binding.appBar.setExpanded(false)
        binding.playlistHeader.onChangedListener = { title, desc ->
            listCode?.let { listCode ->
                viewModel.playlist.modifyPlaylist(listCode, title, desc, delete = false)
            }
        }
        binding.playlistHeader.onDeleteActionListener = {
            listCode?.let { listCode ->
                viewModel.playlist.modifyPlaylist(
                    listCode, listTitle.orEmpty(), listDesc.orEmpty(), delete = true
                )
            }
        }
    }
}