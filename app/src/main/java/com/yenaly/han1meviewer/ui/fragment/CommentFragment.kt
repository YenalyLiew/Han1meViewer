package com.yenaly.han1meviewer.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.COMMENT_TYPE
import com.yenaly.han1meviewer.VIDEO_COMMENT_PREFIX
import com.yenaly.han1meviewer.databinding.FragmentCommentBinding
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.adapter.VideoCommentRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.CommentViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.arguments
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 21:09
 */
class CommentFragment : YenalyFragment<FragmentCommentBinding, CommentViewModel>() {

    private val commentTypePrefix by arguments(COMMENT_TYPE, VIDEO_COMMENT_PREFIX)
    private val commentAdapter by unsafeLazy { VideoCommentRvAdapter() }

    override fun initData(savedInstanceState: Bundle?) {
        binding.rvComment.layoutManager = LinearLayoutManager(context)
        binding.rvComment.adapter = commentAdapter
        binding.srlComment.setOnRefreshListener {
            getHanimeVideoComment(commentTypePrefix, viewModel.code)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun liveDataObserve() {
        lifecycleScope.launch {
            whenStarted {
                viewModel.videoCommentFlow.collect { state ->
                    binding.rvComment.isGone = state !is WebsiteState.Success
                    binding.errorTip.isVisible = state is WebsiteState.Error
                    when (state) {
                        is WebsiteState.Error -> {
                            binding.srlComment.finishRefresh()
                            binding.errorTip.text = "🥺\n${state.throwable.message}"
                        }
                        is WebsiteState.Loading -> {
                            binding.srlComment.autoRefresh()
                        }
                        is WebsiteState.Success -> {
                            binding.srlComment.finishRefresh()
                            commentAdapter.setList(state.info)
                            binding.rvComment.isGone = state.info.isEmpty()
                            binding.tvCommentNotFound.isVisible = state.info.isEmpty()
                        }
                    }
                }
            }
        }
    }

    private fun getHanimeVideoComment(type: String, code: String) =
        viewModel.getComment(type, code)
}