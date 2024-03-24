package com.yenaly.han1meviewer.ui.fragment.video

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.LinearLayoutManager
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.yenaly.han1meviewer.COMMENT_TYPE
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_COMMENT_PREFIX
import com.yenaly.han1meviewer.databinding.FragmentCommentBinding
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.adapter.VideoCommentRvAdapter
import com.yenaly.han1meviewer.ui.popup.ReplyPopup
import com.yenaly.han1meviewer.ui.viewmodel.CommentViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.arguments
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 21:09
 */
class CommentFragment : YenalyFragment<FragmentCommentBinding, CommentViewModel>(),
    StateLayoutMixin {

    private val commentTypePrefix by arguments(COMMENT_TYPE, VIDEO_COMMENT_PREFIX)
    private val commentAdapter by unsafeLazy {
        VideoCommentRvAdapter(this)
    }
    private val replyPopup by unsafeLazy {
        ReplyPopup(requireContext()).also { it.hint = getString(R.string.comment) }
    }

    override fun initData(savedInstanceState: Bundle?) {
        binding.state.init {
            onEmpty {
                findViewById<TextView>(R.id.tv_empty).setText(R.string.comment_not_found)
            }
        }

        binding.rvComment.layoutManager = LinearLayoutManager(context)
        binding.rvComment.adapter = commentAdapter
        binding.srlComment.setOnRefreshListener {
            viewModel.getComment(commentTypePrefix, viewModel.code)
        }
        binding.btnComment.isVisible = isAlreadyLogin
        replyPopup.setOnSendListener {
            viewModel.currentUserId?.let { id ->
                viewModel.postComment(
                    id,
                    viewModel.code, commentTypePrefix, replyPopup.comment
                )
            } ?: showShortToast(R.string.there_is_a_small_issue)
        }
        binding.btnComment.setOnClickListener {
            XPopup.Builder(context).autoOpenSoftInput(true)
                .setPopupCallback(object : SimpleCallback() {
                    override fun beforeShow(popupView: BasePopupView?) {
                        binding.btnComment.hide()
                    }

                    override fun onDismiss(popupView: BasePopupView?) {
                        binding.btnComment.show()
                    }
                }).asCustom(replyPopup).show()
        }
    }

    override fun bindDataObservers() {
        lifecycleScope.launch {
            whenStarted {
                viewModel.videoCommentFlow.collect { state ->
                    binding.rvComment.isGone = state is WebsiteState.Error
                    when (state) {
                        is WebsiteState.Error -> {
                            binding.srlComment.finishRefresh()
                            binding.state.showError(state.throwable)
                        }

                        is WebsiteState.Loading -> {
                            binding.srlComment.autoRefresh()
                        }

                        is WebsiteState.Success -> {
                            binding.srlComment.finishRefresh()
                            viewModel.csrfToken = state.info.csrfToken
                            viewModel.currentUserId = state.info.currentUserId
                            commentAdapter.submitList(state.info.videoComment)
                            binding.rvComment.isGone = state.info.videoComment.isEmpty()
                            if (state.info.videoComment.isEmpty()) {
                                binding.state.showEmpty()
                            } else {
                                binding.state.showContent()
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            whenStarted {
                viewModel.postCommentFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Error -> {
                            showShortToast(R.string.send_failed)
                        }

                        is WebsiteState.Loading -> {
                            showShortToast(R.string.sending_comment)
                        }

                        is WebsiteState.Success -> {
                            showShortToast(R.string.send_success)
                            viewModel.getComment(commentTypePrefix, viewModel.code)
                            replyPopup.dismiss()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            whenStarted {
                viewModel.postReplyFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Error -> {
                            showShortToast(R.string.send_failed)
                        }

                        is WebsiteState.Loading -> {
                            showShortToast(R.string.sending_reply)
                        }

                        is WebsiteState.Success -> {
                            showShortToast(R.string.send_success)
                            viewModel.getComment(commentTypePrefix, viewModel.code)
                            commentAdapter.replyPopup?.dismiss()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            whenStarted {
                viewModel.commentLikeFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Error -> showShortToast(state.throwable.message)
                        is WebsiteState.Loading -> Unit
                        is WebsiteState.Success -> viewModel.handleCommentLike(
                            state.info, commentAdapter
                        )
                    }
                }
            }
        }
    }
}