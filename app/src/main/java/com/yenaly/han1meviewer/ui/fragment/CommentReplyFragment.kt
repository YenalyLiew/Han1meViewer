package com.yenaly.han1meviewer.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.COMMENT_ID
import com.yenaly.han1meviewer.CSRF_TOKEN
import com.yenaly.han1meviewer.databinding.FragmentCommentReplyBinding
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.adapter.VideoCommentRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.CommentViewModel
import com.yenaly.yenaly_libs.base.YenalyBottomSheetDialogFragment
import com.yenaly.yenaly_libs.utils.arguments
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/21 021 22:58
 */
class CommentReplyFragment :
    YenalyBottomSheetDialogFragment<FragmentCommentReplyBinding>() {

    val commentId by arguments<String>(COMMENT_ID)
    val csrfToken by arguments<String>(CSRF_TOKEN)
    val viewModel by viewModels<CommentViewModel>()
    private val replyAdapter by unsafeLazy {
        VideoCommentRvAdapter(this).apply { setDiffCallback(VideoCommentRvAdapter.COMPARATOR) }
    }

    override fun initData(savedInstanceState: Bundle?, dialog: Dialog) {
        if (commentId == null) dialog.dismiss()

        viewModel.csrfToken = csrfToken
        binding.rvReply.layoutManager = LinearLayoutManager(context)
        binding.rvReply.adapter = replyAdapter

        viewModel.getCommentReply(commentId!!)

        lifecycleScope.launch {
            whenStarted {
                viewModel.videoReplyFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Error -> {
                            showShortToast("加載回覆失敗了捏")
                            dialog.dismiss()
                        }
                        is WebsiteState.Loading -> {

                        }
                        is WebsiteState.Success -> {
                            replyAdapter.setDiffNewData(state.info.videoComment)
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
                            showShortToast("發送失敗！")
                        }
                        is WebsiteState.Loading -> {
                            showShortToast("發表回覆中")
                        }
                        is WebsiteState.Success -> {
                            showShortToast("發送成功！")
                            viewModel.getCommentReply(commentId!!)
                            replyAdapter.commentPopup?.dismiss()
                        }
                    }
                }
            }
        }
    }
}