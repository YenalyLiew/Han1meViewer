package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Card
import com.drakeet.about.Category
import com.drakeet.about.Contributor
import com.google.android.material.appbar.AppBarLayout
import com.yenaly.circularrevealswitch.SwitchAnimation
import com.yenaly.circularrevealswitch.ext.setDayNightModeSwitcher
import com.yenaly.han1meviewer.BuildConfig
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.showSnackBar

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/06 006 22:42
 */
class AboutActivity : AbsAboutActivity() {

    private val eggArray = arrayOf(
        "為什麼點擊這裡，以為這裡有彩蛋嗎？",
        "別點了！！",
        "再點擊 4 次進入崩壞模式",
        "再點擊 3 次進入崩壞模式",
        "再點擊 2 次進入崩壞模式",
        "再點擊 1 次進入崩壞模式",
        "逗你玩的，你還真信了！",
    )

    private var clickIconTimes = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {

        val randomHeaderColor = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(
                randomHeaderColor,
                Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
            )
        ).apply {
            cornerRadius = 0F
        }

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (ColorUtils.calculateLuminance(randomHeaderColor) > 0.5) {
            slogan.setTextColor(Color.BLACK)
            version.setTextColor(Color.BLACK)
            controller.isAppearanceLightStatusBars = true
        } else {
            slogan.setTextColor(Color.WHITE)
            version.setTextColor(Color.WHITE)
            controller.isAppearanceLightStatusBars = false
        }

        findViewById<AppBarLayout>(com.drakeet.about.R.id.header_layout).apply {
            setBackgroundColor(randomHeaderColor)
            setDayNightModeSwitcher(
                duration = 1000L,
                animToDayMode = SwitchAnimation.entries.random(),
                animToNightMode = SwitchAnimation.entries.random()
            )
        }
        setHeaderContentScrim(gradientDrawable)

        icon.load(R.drawable.icon_transparent_han1me_viewer_rurires) {
            transformations(CircleCropTransformation())
        }
        icon.setOnClickListener {
            showSnackBar(eggArray[clickIconTimes++ % eggArray.size])
        }
        slogan.setText(R.string.app_slogan)
        version.text = "v${BuildConfig.VERSION_NAME}"
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.apply {
            add(Category("介紹"))
            add(
                Card(
                    """
                Hanime1的非官方客戶端，提供了等同Hanime1網站的大部分功能。
                並且提供了一些獨特的功能。
                請注意：本軟體僅供學習使用，軟體作者與網站擁有者無關。
            """.trimIndent()
                )
            )

            add(Category("貢獻者"))
            add(
                Contributor(
                    R.drawable.yenaly_liew,
                    "Yenaly",
                    "Developer",
                    "https://github.com/YenalyLiew"
                )
            )
            add(
                Contributor(
                    0,
                    "rurires",
                    "Icon creator",
                    "https://github.com/rurires"
                )
            )
            add(
                Contributor(
                    R.drawable.neko_ouo,
                    "NeKoOuO",
                    "Contributor",
                    "https://github.com/NeKoOuO"
                )
            )
        }
    }
}
