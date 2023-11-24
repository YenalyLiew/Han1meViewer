package com.yenaly.han1meviewer.ui.fragment.settings

import android.os.Bundle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentHKeyframesBinding
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.adapter.HKeyframesRvAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.viewmodel.SettingsViewModel
import com.yenaly.han1meviewer.util.resetEmptyView
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.decodeFromStringByBase64
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.textFromClipboard
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.concurrent.thread

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/13 013 18:46
 */
class HKeyframesFragment :
    YenalyFragment<FragmentHKeyframesBinding, SettingsViewModel>(),
    IToolbarFragment<SettingsActivity> {

    private val adapter by unsafeLazy { HKeyframesRvAdapter() }

    private val hKeyframesShareRegex = Regex(">>>(.+)<<<")

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun initData(savedInstanceState: Bundle?) {
        binding.rvKeyframe.layoutManager = LinearLayoutManager(context)
        binding.rvKeyframe.adapter = adapter
        adapter.resetEmptyView(R.layout.layout_empty_view)
        adapter.setDiffCallback(HKeyframesRvAdapter.COMPARATOR)
    }

    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadAllHKeyframes().flowWithLifecycle(lifecycle)
                .collect { entity ->
                    adapter.setDiffNewData(entity)
                }
        }
    }

    private fun addHKeyframes() {
        thread {
            textFromClipboard?.let { text ->
                val matchResult = hKeyframesShareRegex.find(text)
                if (matchResult != null) {
                    val (toBase64) = matchResult.destructured
                    val toJson = toBase64.decodeFromStringByBase64()
                    val entity = Json.decodeFromString<HKeyframeEntity>(toJson)
                    activity?.runOnUiThread {
                        context?.showAlertDialog {
                            setTitle(R.string.h_keyframes_shared_by_other_detected)
                            setMessage(
                                """
                                注意：如果你也有和對方相同代號的關鍵H幀，那麼你自己的將會被覆蓋。
                                
                                標題：${entity.title}
                                代號：${entity.videoCode}
                                有 ${entity.keyframes.size} 個時刻
                            """.trimIndent()
                            )
                            setPositiveButton(R.string.confirm) { _, _ ->
                                viewModel.insertHKeyframes(entity.copy(lastModifiedTime = System.currentTimeMillis()))
                            }
                            setNegativeButton(R.string.cancel, null)
                        }
                    }

                } else {
                    activity?.runOnUiThread {
                        showShortToast(R.string.h_keyframes_shared_by_other_not_detected)
                    }
                }
            } ?: activity?.runOnUiThread {
                showShortToast(R.string.h_keyframes_shared_by_other_not_detected)
            }
        }
    }

    override fun SettingsActivity.setupToolbar() {
        supportActionBar!!.setTitle(R.string.h_keyframe_manage)
        this@HKeyframesFragment.apply {
            addMenu(R.menu.menu_h_keyframes_toolbar, viewLifecycleOwner) {
                when (it.itemId) {
                    R.id.tb_add -> addHKeyframes()
                }
                return@addMenu true
            }
        }
    }
}