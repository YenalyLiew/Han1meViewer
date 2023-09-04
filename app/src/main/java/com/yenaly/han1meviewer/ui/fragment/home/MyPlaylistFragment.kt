package com.yenaly.han1meviewer.ui.fragment.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.SIMPLIFIED_VIDEO_IN_ONE_LINE
import com.yenaly.han1meviewer.databinding.FragmentPlaylistBinding
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeMyListVideoAdapter
import com.yenaly.han1meviewer.ui.adapter.PlaylistRvAdapter
import com.yenaly.han1meviewer.ui.fragment.ILoginNeededFragment
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.han1meviewer.util.resetEmptyView
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.clickTrigger
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:43
 */
class MyPlaylistFragment : YenalyFragment<FragmentPlaylistBinding, MyListViewModel>(),
    IToolbarFragment<MainActivity>, ILoginNeededFragment {

    private var page: Int
        set(value) {
            viewModel.playlistPage = value
        }
        get() = viewModel.playlistPage

    var listCode: String?
        set(value) {
            viewModel.playlistCode = value
            binding.playlistHeader.isVisible = value != null
        }
        get() = viewModel.playlistCode

    var listTitle: String?
        set(value) {
            viewModel.playlistTitle = value
            binding.playlistHeader.title = value
        }
        get() = viewModel.playlistTitle

    private var listDesc: String?
        set(value) {
            viewModel.playlistDesc = value
            binding.playlistHeader.description = value
        }
        get() = viewModel.playlistDesc

    private val adapter by unsafeLazy { HanimeMyListVideoAdapter() }
    private val playlistsAdapter by unsafeLazy { PlaylistRvAdapter(this) }

    private val emptyView by unsafeLazy {
        LayoutInflater.from(context).inflate(
            R.layout.layout_empty_view,
            adapter.recyclerViewOrNull,
            false
        )
    }

    private val emptyViewForPlaylists by unsafeLazy {
        LayoutInflater.from(context).inflate(
            R.layout.layout_empty_view,
            adapter.recyclerViewOrNull,
            false
        )
    }

    @SuppressLint("InflateParams")
    override fun initData(savedInstanceState: Bundle?) {
        checkLogin()
        (activity as MainActivity).setupToolbar()

        initPlaylistHeader()

        viewModel.getPlaylists()
        getNewPlaylistItems()

        adapter.setOnItemLongClickListener { _, _, position ->
            val item = adapter.getItem(position)
            requireContext().showAlertDialog {
                setTitle("刪除播放清單")
                setMessage(getString(R.string.sure_to_delete_s_video, item.title))
                setPositiveButton(R.string.confirm) { _, _ ->
                    listCode?.let { listCode ->
                        viewModel.deletePlaylist(listCode, item.videoCode, position)
                    }
                }
                setNegativeButton(R.string.cancel, null)
            }
            return@setOnItemLongClickListener true
        }

        binding.rvPageList.apply {
            layoutManager = GridLayoutManager(context, SIMPLIFIED_VIDEO_IN_ONE_LINE)
            adapter = this@MyPlaylistFragment.adapter
        }
        binding.rvPlaylist.layoutManager = LinearLayoutManager(context)
        binding.rvPlaylist.adapter = playlistsAdapter

        binding.btnRefreshPlaylists.clickTrigger(viewLifecycleOwner.lifecycle) {
            viewModel.getPlaylists()
        }
        binding.btnNewPlaylist.setOnClickListener {
            requireContext().showAlertDialog {
                setTitle("創建新清單")
                val etView =
                    LayoutInflater.from(context)
                        .inflate(R.layout.layout_playlist_modify_edit_text, null)
                val etTitle = etView.findViewById<EditText>(R.id.et_title)
                val etDesc = etView.findViewById<EditText>(R.id.et_desc)
                setView(etView)
                setPositiveButton(R.string.confirm) { _, _ ->
                    viewModel.createPlaylist(etTitle.text.toString(), etDesc.text.toString())
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
            whenStarted {
                viewModel.playlistFlow.collect { state ->
                    when (state) {
                        is PageLoadingState.Error -> {
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(false)
                            // set error view
                            adapter.resetEmptyView(emptyView, "🥺\n${state.throwable.message}")
                        }

                        is PageLoadingState.Loading -> {
                            adapter.removeEmptyView()
                            if (listCode == null) {
                                adapter.resetEmptyView(emptyView, "請從右向左滑動選擇列表")
                            } else if (adapter.data.isEmpty()) {
                                binding.srlPageList.autoRefreshAnimationOnly()
                            }
                        }

                        is PageLoadingState.NoMoreData -> {
                            binding.srlPageList.finishLoadMoreWithNoMoreData()
                            binding.srlPageList.finishRefresh()
                            if (adapter.data.isEmpty()) {
                                adapter.notifyDataSetChanged() // 這裡要用notifyDataSetChanged()，不然不會出現空白頁，而且crash
                                adapter.resetEmptyView(emptyView)
                            }
                        }

                        is PageLoadingState.Success -> {
                            page++
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(true)
                            viewModel.csrfToken = state.info.csrfToken
                            Log.d("csrf_token", viewModel.csrfToken.toString())
                            listDesc = state.info.desc
                            Log.d("playlist", state.info.hanimeInfo.toString())
                            if (listCode != null) adapter.addData(state.info.hanimeInfo)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.playlistsFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Success -> {
                            viewModel.csrfToken = state.info.csrfToken
                            playlistsAdapter.setList(state.info.playlists)
                        }

                        is WebsiteState.Error -> {
                            playlistsAdapter.resetEmptyView(
                                emptyViewForPlaylists,
                                "🥺\n${state.throwable.message}"
                            )
                        }

                        is WebsiteState.Loading -> {
                            playlistsAdapter.resetEmptyView(emptyViewForPlaylists, "加載中...")
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deletePlaylistFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast("刪除失敗！")
                    }

                    is WebsiteState.Loading -> {
                    }

                    is WebsiteState.Success -> {
                        val index = state.info
                        showShortToast("刪除成功！")
                        adapter.removeAt(index)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.modifyPlaylistFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast("修改失敗！")
                    }

                    is WebsiteState.Loading -> {
                    }

                    is WebsiteState.Success -> {
                        showShortToast("修改成功！")
                        Log.d("asd", state.info.toString())
                        if (state.info.isDeleted) {
                            listCode = null
                            listTitle = null
                            listDesc = null
                            adapter.setList(null)
                            adapter.resetEmptyView(emptyView, "請從右向左滑動選擇列表")
                        } else {
                            listTitle = state.info.title
                            listDesc = state.info.desc
                        }
                        viewModel.getPlaylists()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createPlaylistFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast("添加失敗！")
                    }

                    is WebsiteState.Loading -> Unit
                    is WebsiteState.Success -> {
                        showShortToast("添加成功！")
                        viewModel.getPlaylists()
                    }
                }
            }
        }
    }

    private fun getPlaylistItems() {
        val listCode = listCode
        if (listCode != null) {
            viewModel.getPlaylistItems(page, listCode = listCode)
        } else {
            adapter.resetEmptyView(emptyView, "請從右向左滑動選擇列表")
        }
    }

    fun getNewPlaylistItems() {
        page = 1
        adapter.data.clear()
        getPlaylistItems()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.rvPageList.layoutManager = GridLayoutManager(context, SIMPLIFIED_VIDEO_IN_ONE_LINE)
    }

    override fun MainActivity.setupToolbar() {
        val toolbar = this@MyPlaylistFragment.binding.toolbar
        val dlPlaylist = this@MyPlaylistFragment.binding.dlPlaylist
        setSupportActionBar(toolbar)
        supportActionBar!!.setSubtitle(R.string.play_list)
        this@MyPlaylistFragment.addMenu(
            R.menu.menu_playlist_toolbar,
            viewLifecycleOwner
        ) { menuItem ->
            when (menuItem.itemId) {
                R.id.tb_open_drawer -> dlPlaylist.openDrawer(GravityCompat.END)
            }
            return@addMenu false
        }

        toolbar.setupWithMainNavController()
    }

    private fun initPlaylistHeader() {
        binding.playlistHeader.onChangedListener = { title, desc ->
            listCode?.let { listCode ->
                viewModel.modifyPlaylist(listCode, title, desc, delete = false)
            }
        }
        binding.playlistHeader.onDeleteActionListener = {
            listCode?.let { listCode ->
                viewModel.modifyPlaylist(
                    listCode, listTitle.orEmpty(), listDesc.orEmpty(), delete = true
                )
            }
        }
    }
}