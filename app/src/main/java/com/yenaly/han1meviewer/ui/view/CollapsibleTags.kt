package com.yenaly.han1meviewer.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.yenaly.han1meviewer.FROM_VIDEO_TAG
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.copyToClipboard
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity

/**
 * 可折叠 TAG 栏
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/06 006 21:46
 */
class CollapsibleTags @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {


    companion object {
        val animInterpolator = FastOutSlowInInterpolator()
        const val animDuration = 300L
    }

    /**
     * 设置当前是否折叠，同时作为监听器，
     * 修改这里的值会改变折叠状态
     */
    var isCollapsed = true
        set(value) {
            field = value
            handleWhenCollapsed(value)
        }

    private var tagViewList: MutableList<Chip>? = null
        set(value) {
            field = value
            chipGroup.removeAllViews()
            value?.forEach(chipGroup::addView)
            chipGroupMeasureHeight = chipGroup.calcHeight()
            collapseValueAnimator = buildChipGroupAnimator(chipGroupMeasureHeight, 0)
            expandValueAnimator = buildChipGroupAnimator(0, chipGroupMeasureHeight)
        }

    private var chipGroupMeasureHeight = 0
    private var collapseValueAnimator: ValueAnimator? = null
    private var expandValueAnimator: ValueAnimator? = null

    private val tagCardView: MaterialCardView
    private val toggleButton: MaterialButton
    private val chipGroup: ChipGroup

    init {
        inflate(context, R.layout.layout_collapsible_tag, this)
        tagCardView = findViewById(R.id.tag_card_view)
        toggleButton = findViewById(R.id.toggle_button)
        chipGroup = findViewById(R.id.tag_group)

        // default
        chipGroup.visibility = GONE
        toggleButton.setOnClickListener {
            isCollapsed = !isCollapsed
        }
    }

    /**
     * 從這裏設置tags
     *
     * post很重要，因為要等到View被加入到Window才能取得父View的寬度，
     * 在RecyclerView中不這麽設置會出現問題。
     *
     * @param tags 標籤列表
     */
    fun setTags(tags: List<String>) = post {
        setTagsInternal(tags)
    }

    private fun setTagsInternal(tags: List<String>) {
        tagViewList = tags.map { tag ->
            (LayoutInflater.from(context)
                .inflate(R.layout.item_video_tag_chip, this, false) as Chip)
                .apply {
                    text = tag
                    setOnClickListener {
                        context?.activity?.startActivity<SearchActivity>(FROM_VIDEO_TAG to tag)
                    }
                    setOnLongClickListener {
                        tag.copyToClipboard()
                        // todo: strings.xml
                        showShortToast("「$tag」已複製到剪貼簿")
                        return@setOnLongClickListener true
                    }
                }
        }.toMutableList()
    }

    private fun handleWhenCollapsed(isCollapsed: Boolean) {
        toggleButton.animate()
            .rotation(if (isCollapsed) 0F else 180F)
            .setDuration(animDuration)
            .setInterpolator(animInterpolator)
            .start()


        if (isCollapsed) {
            chipGroup.animate()
                .setDuration(animDuration)
                .setInterpolator(animInterpolator)
                .alpha(0F)
                .withStartAction {
                    collapseValueAnimator?.start()
                }.withEndAction {
                    chipGroup.visibility = GONE
                }.start()
        } else {
            chipGroup.animate()
                .setDuration(animDuration)
                .setInterpolator(animInterpolator)
                .alpha(1F)
                .withStartAction {
                    chipGroup.visibility = VISIBLE
                    expandValueAnimator?.start()
                }.start()
        }
    }

    private fun View.calcHeight(): Int {
        val matchParentMeasureSpec =
            MeasureSpec.makeMeasureSpec((parent as View).width, MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        return measuredHeight
    }

    private fun buildChipGroupAnimator(start: Int, end: Int): ValueAnimator {
        return ValueAnimator.ofInt(start, end).apply {
            duration = animDuration
            interpolator = animInterpolator
            addUpdateListener {
                val value = it.animatedValue as Int
                chipGroup.updateLayoutParams {
                    height = value
                }
            }
        }
    }
}