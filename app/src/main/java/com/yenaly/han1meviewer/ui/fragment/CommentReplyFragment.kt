package com.yenaly.han1meviewer.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.COMMENT_ID
import com.yenaly.han1meviewer.databinding.FragmentCommentReplyBinding
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.adapter.VideoCommentRvAdapter
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

    private val commentId by arguments<String>(COMMENT_ID)
    private val replyAdapter by unsafeLazy { VideoCommentRvAdapter() }

    override fun initData(savedInstanceState: Bundle?, dialog: Dialog) {
        if (commentId == null) dialog.dismiss()

        binding.rvReply.layoutManager = LinearLayoutManager(context)
        binding.rvReply.adapter = replyAdapter

        lifecycleScope.launch {
            whenCreated {
                NetworkRepo.getCommentReply(commentId!!).collect { state ->
                    when (state) {
                        is WebsiteState.Error -> {
                            showShortToast("加載回覆失敗了捏")
                            dialog.dismiss()
                        }
                        is WebsiteState.Loading -> {

                        }
                        is WebsiteState.Success -> {
                            replyAdapter.setList(state.info)
                        }
                    }
                }
            }
        }
    }
}