package com.yenaly.han1meviewer.ui.fragment.search

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.impl.CenterListPopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.lxj.xpopupext.listener.TimePickerListener
import com.lxj.xpopupext.popup.TimePickerPopup
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.SEARCH_YEAR_RANGE_END
import com.yenaly.han1meviewer.SEARCH_YEAR_RANGE_START
import com.yenaly.han1meviewer.databinding.PopUpFragmentSearchOptionsBinding
import com.yenaly.han1meviewer.ui.popup.HanimeSearchTagCenterPopup
import com.yenaly.han1meviewer.ui.viewmodel.SearchViewModel
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyBottomSheetDialogFragment
import java.util.Calendar
import java.util.Date

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/08 008 17:04
 */
class SearchOptionsPopupFragment :
    YenalyBottomSheetDialogFragment<PopUpFragmentSearchOptionsBinding>() {

    companion object Tags {

        private const val POP_UP_BORDER_RADIUS = 36F

        // type 就是 genre
        private val typeArray =
            arrayOf("全部", "裏番", "泡麵番", "Motion Anime", "3D動畫", "同人作品", "Cosplay")

        private val videoAttrTagArray =
            arrayOf("無碼", "AI解碼", "中文字幕", "1080p", "60FPS")
        private val relationshipTagArray =
            arrayOf("近親", "姐", "妹", "母", "女兒", "師生", "情侶", "青梅竹馬")
        private val characterSettingTagArray =
            arrayOf(
                "JK", "處女", "御姐", "熟女", "人妻", "老師", "女醫護士",
                "OL", "大小姐", "偶像", "女僕", "巫女", "修女", "風俗娘", "公主",
                "女戰士", "魔法少女", "異種族", "妖精", "魔物娘",
                "獸娘", "碧池", "痴女", "不良少女", "傲嬌",
                "病嬌", "無口", "偽娘", "扶他"
            )
        private val appearanceTagArray =
            arrayOf(
                "短髮", "馬尾", "雙馬尾", "巨乳", "貧乳",
                "黑皮膚", "眼鏡娘", "獸耳", "美人痣", "肌肉女", "白虎",
                "大屌", "水手服", "體操服", "泳裝", "比基尼", "和服",
                "兔女郎", "圍裙", "啦啦隊", "旗袍", "絲襪", "吊襪帶",
                "熱褲", "迷你裙", "性感內衣", "丁字褲", "高跟鞋", "淫紋"
            )
        private val storyPlotTagArray =
            arrayOf(
                "純愛", "戀愛喜劇", "後宮", "開大車", "公眾場合",
                "NTR", "精神控制", "藥物", "痴漢", "阿嘿顏", "精神崩潰",
                "獵奇", "BDSM", "綑綁", "眼罩", "項圈", "調教", "異物插入",
                "肉便器", "胃凸", "強制", "逆強制", "女王樣", "母女丼", "姐妹丼",
                "凌辱", "出軌", "攝影", "性轉換", "百合", "耽美", "異世界", "怪獸", "世界末日"
            )
        private val sexPositionTagArray =
            arrayOf(
                "手交", "指交", "乳交", "肛交", "腳交", "拳交", "3P",
                "群交", "口交", "口爆", "吞精", "舔蛋蛋", "舔穴", "69",
                "自慰", "腋毛", "腋交", "舔腋下", "內射", "顏射", "雙洞齊下",
                "懷孕", "噴奶", "放尿", "排便", "顏面騎乘", "車震", "性玩具",
                "毒龍鑽", "觸手", "頸手枷"
            )

        private val sortOptionArray =
            arrayOf(
                "最新上市", "最新上傳", "本日排行",
                "本週排行", "本月排行", "觀看次數", "他們在看"
            )

        private val brandArray =
            arrayOf(
                "妄想実現めでぃあ", "メリー・ジェーン", "ピンクパイナップル",
                "ばにぃうぉ～か～", "Queen Bee", "PoRO", "せるふぃっしゅ",
                "鈴木みら乃", "ショーテン", "GOLD BEAR", "ZIZ", "EDGE",
                "Collaboration Works", "BOOTLEG", "BOMB!CUTE!BOMB!", "nur",
                "あんてきぬすっ", "魔人", "ルネ", "Princess Sugar", "パシュミナ",
                "WHITE BEAR", "AniMan", "chippai", "トップマーシャル", "erozuki",
                "サークルトリビュート", "spermation", "Milky", "King Bee", "PashminaA",
                "じゅうしぃまんご～", "Hills", "妄想専科", "ディスカバリー", "ひまじん",
                "37℃", "schoolzone", "GREEN BUNNY", "バニラ", "L.", "PIXY", "こっとんど～る",
                "ANIMAC", "Celeb", "MOON ROCK", "Dream", "ミンク", "オズ・インク",
                "サン出版", "ポニーキャニオン", "わるきゅ～れ＋＋", "株式会社虎の穴",
                "エンゼルフィッシュ", "UNION-CHO", "TOHO", "ミルクセーキ", "2匹目のどぜう",
                "じゅうしぃまんご～", "ツクルノモリ", "サークルトリビュート",
                "トップマーシャル", "サークルトリビュート", "彗星社", "ナチュラルハイ",
                "れもんは～と"
            )

        private val durationMap =
            linkedMapOf(
                "全部" to null,
                "短片（4分鐘內）" to "短片",
                "中長片（4至20分鐘）" to "中長片",
                "長片（20分鐘以上）" to "長片"
            )
    }

    private val viewModel by activityViewModels<SearchViewModel>()

    // Popups

    private val brandPopup: HanimeSearchTagCenterPopup
        get() {
            val popup = HanimeSearchTagCenterPopup(requireContext()).apply {
                title = getString(R.string.brand)
                selectedTagSet = viewModel.brandSet
                isPairWidelyLayoutShown = false
                addTagsScope {
                    addTagGroup(null, brandArray)
                }
                setOnResetClickListener {
                    clearAllChecks()
                }
                setOnSaveClickListener {
                    viewModel.brandSet.clear()
                    viewModel.brandSet += selectedTagSet
                    initOptionsChecked()
                    dismiss()
                }
            }
            return XPopup.Builder(requireContext()).setOptionsCheckedCallback()
                .borderRadius(POP_UP_BORDER_RADIUS)
                .asCustom(popup) as HanimeSearchTagCenterPopup
        }

    @Suppress("unused")
    @Deprecated("with no reason")
    private val sortOptionPopup: CenterListPopupView
        get() {
            val index = sortOptionArray.indexOf(viewModel.sort)
            return XPopup.Builder(requireContext()).setOptionsCheckedCallback()
                .borderRadius(POP_UP_BORDER_RADIUS)
                .isDarkTheme(true)
                .asCenterList(
                    getString(R.string.sort_option_able_reset),
                    sortOptionArray, null,
                    index
                ) { position, text ->
                    viewModel.sort = if (index != position) text else null
                    initOptionsChecked()
                }!!
        }

    @Suppress("unused")
    @Deprecated("with no reason")
    private val durationPopup: CenterListPopupView
        get() {
            val durations = durationMap.keys.toTypedArray()
            val index = durations.indexOf(viewModel.duration)
            return XPopup.Builder(requireContext()).setOptionsCheckedCallback()
                .borderRadius(POP_UP_BORDER_RADIUS)
                .isDarkTheme(true)
                .asCenterList(
                    getString(R.string.duration_able_reset),
                    durations, null,
                    index
                ) { position, text ->
                    viewModel.duration = if (index != position) durationMap[text] else null
                    initOptionsChecked()
                }!!
        }

    private val tagPopup: HanimeSearchTagCenterPopup
        get() {
            val popup = HanimeSearchTagCenterPopup(requireContext()).apply {
                title = getString(R.string.tag)
                isPairWidelyLayoutShown = true
                shouldPairWidely = viewModel.broad
                selectedTagSet = viewModel.tagSet
                setOnPairWidelySwitchCheckedListener { _, isChecked ->
                    viewModel.broad = isChecked
                }
                addTagsScope {
                    addTagGroup(getString(R.string.video_attr), videoAttrTagArray)
                    addTagGroup(getString(R.string.relationship), relationshipTagArray)
                    addTagGroup(getString(R.string.characteristics), characterSettingTagArray)
                    addTagGroup(getString(R.string.appearance_and_figure), appearanceTagArray)
                    addTagGroup(getString(R.string.story_plot), storyPlotTagArray)
                    addTagGroup(getString(R.string.sex_position), sexPositionTagArray)
                }
                setOnResetClickListener {
                    clearAllChecks()
                }
                setOnSaveClickListener {
                    viewModel.broad = shouldPairWidely
                    viewModel.tagSet.clear()
                    viewModel.tagSet += selectedTagSet
                    initOptionsChecked()
                    dismiss()
                }
            }
            return XPopup.Builder(requireContext()).setOptionsCheckedCallback()
                .borderRadius(POP_UP_BORDER_RADIUS)
                .asCustom(popup) as HanimeSearchTagCenterPopup
        }

    @Suppress("unused")
    @Deprecated("with no reason")
    private val typePopup: CenterListPopupView
        get() {
            val index = typeArray.indexOf(viewModel.genre)
            return XPopup.Builder(requireContext()).setOptionsCheckedCallback()
                .borderRadius(POP_UP_BORDER_RADIUS)
                .isDarkTheme(true)
                .asCenterList(
                    getString(R.string.type_able_reset),
                    typeArray, null,
                    index
                ) { position, text ->
                    viewModel.genre = if (index != position) text else null
                    initOptionsChecked()
                }!!
        }

    private val timePickerPopup: TimePickerPopup
        get() {
            val date = Calendar.getInstance().also {
                val year = viewModel.year
                val month = viewModel.month
                if (year != null && month != null) {
                    it.set(year, month, 0)
                }
            }
            val popup = HTimePickerPopup(requireContext())
                .apply {
                    setMode(TimePickerPopup.Mode.YM)
                    setYearRange(SEARCH_YEAR_RANGE_START, SEARCH_YEAR_RANGE_END)
                    setDefaultDate(date)
                    setTimePickerListener(object : TimePickerListener {
                        override fun onCancel() = Unit
                        override fun onTimeChanged(date: Date) = Unit

                        override fun onTimeConfirm(date: Date, view: View?) {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            when (mode) {
                                TimePickerPopup.Mode.YM -> {
                                    viewModel.year = calendar.get(Calendar.YEAR)
                                    viewModel.month = calendar.get(Calendar.MONTH) + 1
                                }

                                else -> {
                                    viewModel.year = calendar.get(Calendar.YEAR)
                                    viewModel.month = null
                                }
                            }
                            initOptionsChecked()
                        }
                    })
                }
            return XPopup.Builder(requireContext()).setOptionsCheckedCallback()
                .borderRadius(POP_UP_BORDER_RADIUS)
                .isDarkTheme(true)
                .asCustom(popup) as TimePickerPopup
        }

    override fun initData(savedInstanceState: Bundle?, dialog: Dialog) {
        initOptionsChecked()
        initClick()
    }

    private fun initOptionsChecked() {
        binding.brand.isChecked = viewModel.brandSet.isNotEmpty()
        binding.sortOption.isChecked = viewModel.sort != null
        binding.duration.isChecked = viewModel.duration != null
        binding.tag.isChecked = viewModel.tagSet.isNotEmpty()
        binding.type.isChecked = viewModel.genre != null
        binding.releaseDate.isChecked = viewModel.year != null || viewModel.month != null
    }

    private fun initClick() {
        binding.type.apply {
            setOnClickListener {
                // typePopup.show()
                requireContext().showAlertDialog {
                    val index = typeArray.indexOf(viewModel.genre)
                    setTitle(R.string.type)
                    setSingleChoiceItems(typeArray, index) { _, which ->
                        viewModel.genre = typeArray.getOrNull(which)
                        initOptionsChecked()
                    }
                    setPositiveButton(R.string.save, null)
                    setNeutralButton(R.string.reset) { _, _ ->
                        viewModel.genre = null
                        initOptionsChecked()
                    }
                    setOnDismissListener {
                        initOptionsChecked()
                    }
                }
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.genre = null
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        binding.brand.apply {
            setOnClickListener {
                brandPopup.show()
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.brandSet.clear()
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        binding.tag.apply {
            setOnClickListener {
                tagPopup.show()
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.tagSet.clear()
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        binding.sortOption.apply {
            setOnClickListener {
                // sortOptionPopup.show()
                requireContext().showAlertDialog {
                    val index = sortOptionArray.indexOf(viewModel.sort)
                    setTitle(R.string.sort_option)
                    setSingleChoiceItems(sortOptionArray, index) { _, which ->
                        viewModel.sort = sortOptionArray.getOrNull(which)
                        initOptionsChecked()
                    }
                    setPositiveButton(R.string.save, null)
                    setNeutralButton(R.string.reset) { _, _ ->
                        viewModel.sort = null
                        initOptionsChecked()
                    }
                    setOnDismissListener {
                        initOptionsChecked()
                    }
                }
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.sort = null
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        binding.duration.apply {
            setOnClickListener {
                // durationPopup.show()
                requireContext().showAlertDialog {
                    val durations = durationMap.keys.toTypedArray()
                    val index = durations.indexOf(viewModel.duration)
                    setTitle(R.string.duration)
                    setSingleChoiceItems(durations, index) { _, which ->
                        viewModel.duration = durationMap[durations.getOrNull(which)]
                        initOptionsChecked()
                    }
                    setPositiveButton(R.string.save, null)
                    setNeutralButton(R.string.reset) { _, _ ->
                        viewModel.duration = null
                        initOptionsChecked()
                    }
                    setOnDismissListener {
                        initOptionsChecked()
                    }
                }
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.duration = null
                    initOptionsChecked()
                }
                return@lc true
            }
        }
        binding.releaseDate.apply {
            setOnClickListener {
                timePickerPopup.show()
            }
            setOnLongClickListener lc@{
                showClearAllTagsDialog {
                    viewModel.year = null
                    viewModel.month = null
                    initOptionsChecked()
                }
                return@lc true
            }
        }
    }

    private inline fun Chip.showClearAllTagsDialog(crossinline action: () -> Unit) {
        if (isChecked) {
            context.showAlertDialog {
                setTitle(R.string.alert)
                setMessage(R.string.alert_cancel_all_tags)
                setPositiveButton(R.string.confirm) { _, _ -> action.invoke() }
                setNegativeButton(R.string.cancel, null)
            }
        }
    }

    private fun XPopup.Builder.setOptionsCheckedCallback() = apply {
        setPopupCallback(object : SimpleCallback() {
            override fun beforeDismiss(popupView: BasePopupView?) {
                initOptionsChecked()
            }
        })
    }
}