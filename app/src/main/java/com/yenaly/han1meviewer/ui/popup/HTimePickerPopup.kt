package com.yenaly.han1meviewer.ui.popup

import android.content.Context
import com.google.android.material.button.MaterialButton
import com.lxj.xpopupext.popup.TimePickerPopup
import com.yenaly.han1meviewer.R

/**
 * #issue-161: 高级搜索可以选择年或年月
 */
class HTimePickerPopup(context: Context) : TimePickerPopup(context) {

    private lateinit var btnSwitch: MaterialButton

    var mode: Mode = Mode.YM
        private set

    override fun getImplLayoutId(): Int = R.layout.pop_up_ext_h_time_picker

    override fun onCreate() {
        super.onCreate()
        btnSwitch = findViewById(R.id.btnSwitch)
        btnSwitch.text = when (mode) {
            Mode.YM -> context.getString(R.string.switch_to_year)
            else -> context.getString(R.string.switch_to_year_month)
        }
        btnSwitch.setOnClickListener {
            when (mode) {
                Mode.YM -> setMode(Mode.Y)
                else -> setMode(Mode.YM)
            }
            onCreate()
        }
    }

    override fun setMode(mode: Mode): TimePickerPopup {
        this.mode = mode
        return super.setMode(mode)
    }
}