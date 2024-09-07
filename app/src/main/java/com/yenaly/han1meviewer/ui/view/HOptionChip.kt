package com.yenaly.han1meviewer.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.Checkable
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.use
import androidx.core.graphics.ColorUtils
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.dp
import kotlinx.parcelize.Parcelize

class HOptionChip @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleRes: Int = 0
) : AppCompatTextView(context, attrs, defStyleRes), Checkable {

    private val cornerRadius = 12.dp.toFloat()
    private val unselectedColor = context.getColor(R.color.adv_search_unselected_color)
    private val selectedColor = context.getColor(R.color.adv_search_selected_color)

    private var mIsChecked: Boolean = false

    init {
        context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).use {
            val bgRes = it.getResourceId(0, 0)
            setBackgroundResource(bgRes)
        }
        // Set default properties
        updatePadding(top = 12.dp, bottom = 12.dp)
        setTextColor(Color.WHITE)
        // corner radius drawable
        background = GradientDrawable().apply {
            cornerRadius = this@HOptionChip.cornerRadius
            setColor(unselectedColor)
        }
    }

    private fun animateChipByColorTransition(enable: Boolean) {
        val startColor = if (enable) unselectedColor else selectedColor
        val endColor = if (enable) selectedColor else unselectedColor

        val animator = ValueAnimator.ofArgb(startColor, endColor)
        animator.addUpdateListener { animation ->
            background = GradientDrawable().apply {
                cornerRadius = this@HOptionChip.cornerRadius
                setColor(animation.animatedValue as Int)
            }
        }
        animator.interpolator = FastOutSlowInInterpolator()
        animator.duration = 300
        animator.start()
    }

    var isAvailable: Boolean = true
        set(available) {
            field = available
            isEnabled = available
            val gd = background as? GradientDrawable
            if (available) {
                gd?.setColor(if (isChecked) selectedColor else unselectedColor)
            } else {
                gd?.setColor(ColorUtils.setAlphaComponent(unselectedColor, 0x80))
            }
        }

    override fun setChecked(checked: Boolean) {
        if (mIsChecked == checked) return
        mIsChecked = checked
        animateChipByColorTransition(checked)
    }

    override fun isChecked(): Boolean = mIsChecked

    override fun toggle() {
        isChecked = !isChecked
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState(), isChecked)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        this.isChecked = state.isChecked
    }

    @Parcelize
    data class SavedState(
        val ss: Parcelable?,
        val isChecked: Boolean
    ) : BaseSavedState(ss)
}