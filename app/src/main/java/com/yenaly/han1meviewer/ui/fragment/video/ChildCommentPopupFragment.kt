package com.yenaly.han1meviewer.ui.fragment.video

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.yenaly.han1meviewer.COMMENT_ID
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.PopUpFragmentChildCommentBinding
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.adapter.VideoCommentRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.CommentViewModel
import com.yenaly.han1meviewer.util.setGravity
import com.yenaly.yenaly_libs.base.YenalyBottomSheetDialogFragment
import com.yenaly.yenaly_libs.utils.appScreenHeight
import com.yenaly.yenaly_libs.utils.arguments
import com.yenaly.yenaly_libs.utils.dp
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/21 021 22:58
 */
class ChildCommentPopupFragment :
    YenalyBottomSheetDialogFragment<PopUpFragmentChildCommentBinding>() {

    val commentId by arguments<String>(COMMENT_ID)
    val viewModel by viewModels<CommentViewModel>()
    private val replyAdapter by unsafeLazy {
        VideoCommentRvAdapter(this)
    }

    override fun getViewBinding(layoutInflater: LayoutInflater) =
        PopUpFragmentChildCommentBinding.inflate(layoutInflater)

    override fun initData(savedInstanceState: Bundle?, dialog: Dialog) {
        if (commentId == null) dialog.dismiss()

        binding.root.minimumHeight = appScreenHeight / 2
        binding.rvReply.layoutManager = LinearLayoutManager(context)
        binding.rvReply.adapter = replyAdapter

        viewModel.getCommentReply(commentId!!)

        lifecycleScope.launch {
            viewModel.videoReplyStateFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.load_reply_failed)
                        dialog.dismiss()
                    }

                    is WebsiteState.Loading -> Unit

                    is WebsiteState.Success -> Unit
                }
            }
        }

        lifecycleScope.launch {
            viewModel.videoReplyFlow.collectLatest { list ->
                replyAdapter.submitList(list)
                attachRedDotCount(list.size)
            }
        }

        lifecycleScope.launch {
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
                        viewModel.getCommentReply(commentId!!)
                        replyAdapter.replyPopup?.dismiss()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.commentLikeFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> showShortToast(state.throwable.message)
                    is WebsiteState.Loading -> Unit
                    is WebsiteState.Success -> {
                        viewModel.handleCommentLike(state.info)
                        replyAdapter.notifyItemChanged(state.info.commentPosition, 0)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalBadgeUtils::class)
    private fun attachRedDotCount(count: Int) {
        val badgeDrawable = BadgeDrawable.create(requireContext())
        badgeDrawable.isVisible = count > 0
        badgeDrawable.number = count
        BadgeUtils.attachBadgeDrawable(badgeDrawable, binding.tvChildComment)
        badgeDrawable.setGravity(binding.tvChildComment, Gravity.END, 8.dp)
    }
}