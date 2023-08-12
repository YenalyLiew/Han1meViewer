package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import android.widget.TextView
import coil.load
import coil.transform.CircleCropTransformation
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Card
import com.drakeet.about.Category
import com.drakeet.about.Contributor
import com.google.android.material.appbar.AppBarLayout
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.appLocalVersionName
import com.yenaly.yenaly_libs.utils.dp

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/06 006 22:42
 */
class AboutActivity : AbsAboutActivity() {
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

        findViewById<AppBarLayout>(com.drakeet.about.R.id.header_layout).apply {
            setBackgroundColor(randomHeaderColor)
        }
        setHeaderContentScrim(gradientDrawable)

        icon.load(R.mipmap.ic_launcher) {
            transformations(CircleCropTransformation())
        }
        slogan.setText(R.string.app_slogan)
        version.text = "v$appLocalVersionName"
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.apply {
            add(Category("介紹"))
            add(
                Card(
                    """
                Hanime1的非官方客戶端，提供了等同Hanime1網站的大部分功能。
            """.trimIndent()
                )
            )

            add(Category("貢獻者"))
            add(
                Contributor(
                    R.drawable.yenaly_liew,
                    "Yenaly Liew",
                    "Developer",
                    "https://github.com/YenalyLiew"
                )
            )

            add(Category("未實現或不足"))
            add(Card(
                spannable {
                    "「下載」功能不夠完善".span {
                        quote(
                            Color.rgb(
                                (0..255).random(),
                                (0..255).random(),
                                (0..255).random()
                            ), gapWidth = 4.dp
                        )
                    }
                    newline(2)
                    "「播放清單」功能未實裝".span {
                        quote(
                            Color.rgb(
                                (0..255).random(),
                                (0..255).random(),
                                (0..255).random()
                            ), gapWidth = 4.dp
                        )
                    }
                }
            ))
        }
    }
}