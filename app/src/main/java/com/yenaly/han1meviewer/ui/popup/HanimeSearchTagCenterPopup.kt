package com.yenaly.han1meviewer.ui.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.yenaly.han1meviewer.R

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/16 016 15:05
 */
class HanimeSearchTagCenterPopup(context: Context) : CenterPopupView(context) {

    val chipList = mutableListOf<Chip>()

    lateinit var pairWidely: SwitchMaterial
    private lateinit var titleView: TextView
    private lateinit var pairWidelyLayout: LinearLayout
    private lateinit var tagLayout: LinearLayout
    private lateinit var reset: Button
    private lateinit var save: Button

    private var title: CharSequence? = null
    private var isPairWidelyLayoutShow: Boolean = false
    private var pairWidelySwitchAction: ((CompoundButton, isChecked: Boolean) -> Unit)? = null
    private var saveBtnAction: ((View) -> Unit)? = null
    private var resetBtnAction: ((View) -> Unit)? = null
    private var callback: (LinearLayout.() -> Unit)? = null

    override fun getImplLayoutId() = R.layout.pop_up_hanime_search_tag

    override fun onCreate() {
        super.onCreate()

        titleView = findViewById(R.id.title)
        pairWidelyLayout = findViewById(R.id.pair_widely_layout)
        pairWidely = findViewById(R.id.pair_widely)
        tagLayout = findViewById(R.id.tag_layout)
        reset = findViewById(R.id.reset)
        save = findViewById(R.id.save)

        titleView.text = title
        pairWidelyLayout.isVisible = isPairWidelyLayoutShow
        pairWidelySwitchAction?.let { pairWidely.setOnCheckedChangeListener(it) }
        reset.setOnClickListener(resetBtnAction)
        save.setOnClickListener(saveBtnAction)

        callback?.invoke(tagLayout)
    }

    fun setTitle(title: CharSequence) {
        this.title = title
    }

    fun showPairWidelyLayout(show: Boolean) {
        this.isPairWidelyLayoutShow = show
    }

    fun setOnPairWidelySwitchCheckedListener(action: (CompoundButton, isChecked: Boolean) -> Unit) {
        this.pairWidelySwitchAction = action
    }

    fun setOnResetClickListener(action: (View) -> Unit) {
        this.resetBtnAction = action
    }

    fun setOnSaveClickListener(action: (View) -> Unit) {
        this.saveBtnAction = action
    }

    // 後跟addTagGroup
    fun addTagsScope(action: LinearLayout.() -> Unit) {
        this.callback = action
    }

    fun LinearLayout.addTagGroup(
        subtitle: CharSequence?,
        tagTextList: Array<out CharSequence>,
        action: (CompoundButton, text: CharSequence, isChecked: Boolean) -> Unit
    ) {
        val viewGroup = LayoutInflater.from(context)
            .inflate(R.layout.item_tag_chip_group, this, false) as LinearLayout
        val tagGroup = viewGroup.findViewById<ChipGroup>(R.id.chip_group)
        viewGroup.findViewById<TextView>(R.id.sub_title).apply {
            if (subtitle == null) isVisible = false else text = subtitle
        }
        tagTextList.forEach { tagText ->
            tagGroup.addTag(tagText, action)
        }
        addView(viewGroup)
    }

    private inline fun ChipGroup.addTag(
        text: CharSequence,
        crossinline action: (CompoundButton, text: CharSequence, isChecked: Boolean) -> Unit
    ) {
        val tag = LayoutInflater.from(context)
            .inflate(R.layout.item_tag_chip, this, false) as Chip
        tag.text = text
        tag.setOnCheckedChangeListener { buttonView, isChecked ->
            action.invoke(buttonView, text, isChecked)
        }
        chipList.add(tag)
        addView(tag)
    }

    override fun getMaxHeight(): Int {
        return if (XPopupUtils.isLandscape(context)) {
            (XPopupUtils.getAppWidth(context) * 0.9).toInt()
        } else {
            (XPopupUtils.getAppHeight(context) * 0.9).toInt()
        }
    }
}