package com.yenaly.han1meviewer.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.yenaly.han1meviewer.ADVANCED_SEARCH_MAP
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.util.addUpdateListener
import com.yenaly.han1meviewer.util.readBooleanCompat
import com.yenaly.han1meviewer.util.writeBooleanCompat
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.copyToClipboard
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity
import kotlinx.coroutines.launch


/**
 * 可折叠 TAG 栏
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/06 006 21:46
 */
class CollapsibleTags @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {


    companion object {
        val animInterpolator = FastOutSlowInInterpolator()
        const val animDuration = 300L
    }

    /**
     * 原本用来异步加载 Tag，但有这个必要吗？
     *
     * 每次创建还得多传一个 lifecycle，以后可能把这个砍了
     */
    var lifecycle: Lifecycle? = null

    /**
     * 设置当前是否折叠，同时作为监听器，
     * 修改这里的值会改变折叠状态
     */
    var isCollapsed = false
        set(value) {
            field = value
            post { handleWhenCollapsed(value) }
        }

    var isCollapsedEnabled = true
        set(value) {
            field = value
            toggleButton.isVisible = value
        }

    /**
     * 從這裏設置tags
     *
     * post很重要，因為要等到View被加入到Window才能取得父View的寬度，
     * 在RecyclerView中不這麽設置會出現問題。
     */
    var tags: List<String>? = null
        set(value) {
            field = value
            post {
                setTagsInternal(value ?: emptyList())
            }
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
        toggleButton.isVisible = isCollapsedEnabled
        chipGroup.visibility = VISIBLE
        toggleButton.setOnClickListener {
            isCollapsed = !isCollapsed
        }

        post {
            toggleButton.animate().rotation(if (isCollapsed) 0F else 180F).setDuration(animDuration)
                .setInterpolator(animInterpolator).start()
        }
    }

    private fun setTagsInternal(tags: List<String>) {
        lifecycle?.coroutineScope?.launch {
            tagViewList = tags.map { tag ->
                (LayoutInflater.from(context).inflate(
                    R.layout.item_video_tag_chip, this@CollapsibleTags, false
                ) as Chip).apply {
                    text = tag
                    setOnClickListener {
                        context?.activity?.startActivity<SearchActivity>(ADVANCED_SEARCH_MAP to tag)
                    }
                    setOnLongClickListener {
                        tag.copyToClipboard()
                        showShortToast(context.getString(R.string.s_copy_to_clipboard, tag))
                        return@setOnLongClickListener true
                    }
                }
            }.toMutableList()
        }
    }

    private fun handleWhenCollapsed(isCollapsed: Boolean) {
        toggleButton.animate().rotation(if (isCollapsed) 0F else 180F).setDuration(animDuration)
            .setInterpolator(animInterpolator).start()

        if (isCollapsed) {
            chipGroup.animate().setDuration(animDuration).setInterpolator(animInterpolator)
                .alpha(0F).withStartAction {
                    collapseValueAnimator?.start()
                }.withEndAction {
                    chipGroup.visibility = INVISIBLE
                }.start()
        } else {
            chipGroup.animate().setDuration(animDuration).setInterpolator(animInterpolator)
                .alpha(1F).withStartAction {
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
            addUpdateListener(lifecycle) {
                val value = it.animatedValue as Int
                chipGroup.updateLayoutParams {
                    height = value
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.isCollapsed = isCollapsed
        ss.isCollapsedEnabled = isCollapsedEnabled
        ss.chipGroupMeasureHeight = chipGroupMeasureHeight
        ss.tags = tags
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        this.isCollapsed = state.isCollapsed
        this.isCollapsedEnabled = state.isCollapsedEnabled
        this.chipGroupMeasureHeight = state.chipGroupMeasureHeight
        this.tags = state.tags
    }

    class SavedState : BaseSavedState {

        var isCollapsed: Boolean = false
        var isCollapsedEnabled: Boolean = true
        var chipGroupMeasureHeight: Int = 0
        var tags: List<String>? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            this.isCollapsed = parcel.readBooleanCompat()
            this.isCollapsedEnabled = parcel.readBooleanCompat()
            this.chipGroupMeasureHeight = parcel.readInt()
            this.tags = parcel.createStringArrayList()
        }

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)

                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeBooleanCompat(isCollapsed)
            out.writeBooleanCompat(isCollapsedEnabled)
            out.writeInt(chipGroupMeasureHeight)
            out.writeStringList(tags)
        }
    }
}