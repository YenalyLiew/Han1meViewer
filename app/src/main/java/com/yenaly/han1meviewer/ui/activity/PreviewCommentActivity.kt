package com.yenaly.han1meviewer.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yenaly.han1meviewer.COMMENT_TYPE
import com.yenaly.han1meviewer.DATE_CODE
import com.yenaly.han1meviewer.PREVIEW_COMMENT_PREFIX
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivityPreviewCommentBinding
import com.yenaly.han1meviewer.ui.fragment.video.CommentFragment
import com.yenaly.han1meviewer.ui.viewmodel.CommentViewModel
import com.yenaly.han1meviewer.ui.viewmodel.PreviewCommentPrefetcher
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.makeBundle
import com.yenaly.yenaly_libs.utils.safeIntentExtra

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/06/28 028 12:03
 */
class PreviewCommentActivity : YenalyActivity<ActivityPreviewCommentBinding>() {

    val viewModel by viewModels<CommentViewModel>()

    private val date by safeIntentExtra<String>("date") // 感覺沒必要弄成個常量
    private val dateCode by safeIntentExtra<String>(DATE_CODE)

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityPreviewCommentBinding =
        ActivityPreviewCommentBinding.inflate(layoutInflater)

    override fun setUiStyle() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }

    override fun initData(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeActionContentDescription(R.string.back)
            it.title = getString(R.string.latest_hanime_comment, date)
        }

        viewModel.code = dateCode

        PreviewCommentPrefetcher.here().tag(PreviewCommentPrefetcher.Scope.PREVIEW_COMMENT_ACTIVITY)

        val commentFragment =
            CommentFragment().makeBundle(COMMENT_TYPE to PREVIEW_COMMENT_PREFIX)
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fcv_pre_comment, commentFragment)
        }.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        PreviewCommentPrefetcher.bye(PreviewCommentPrefetcher.Scope.PREVIEW_COMMENT_ACTIVITY)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}