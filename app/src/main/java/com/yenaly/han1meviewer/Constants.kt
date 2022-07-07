package com.yenaly.han1meviewer

import com.yenaly.yenaly_libs.utils.appScreenHeight
import com.yenaly.yenaly_libs.utils.appScreenWidth
import com.yenaly.yenaly_libs.utils.applicationContext

const val EMPTY_STRING = ""

// 動態設置影片卡片長寬

@JvmField
val VIDEO_IN_ONE_LINE_PORTRAIT =
    (appScreenWidth / applicationContext.resources.getDimension(R.dimen.video_cover_width)).toInt()

@JvmField
val VIDEO_IN_ONE_LINE_LANDSCAPE =
    (appScreenHeight / applicationContext.resources.getDimension(R.dimen.video_cover_width)).toInt()

@JvmField
val SIMPLIFIED_VIDEO_IN_ONE_LINE_PORTRAIT =
    (appScreenWidth / applicationContext.resources.getDimension(R.dimen.video_cover_simplified_width)).toInt()

@JvmField
val SIMPLIFIED_VIDEO_IN_ONE_LINE_LANDSCAPE =
    (appScreenHeight / applicationContext.resources.getDimension(R.dimen.video_cover_simplified_width)).toInt()

// 設置發佈日期年份，在搜索的tag裏

/**
 * 發佈日期年份開始於
 */
const val SEARCH_YEAR_RANGE_START = 1990

/**
 * 發佈日期年份結束於
 */
const val SEARCH_YEAR_RANGE_END = 2022

// intent傳值用名稱

const val VIDEO_CODE = "VIDEO_CODE"

const val FROM_VIDEO_TAG = "FROM_VIDEO_TAG"

const val COMMENT_ID = "COMMENT_ID"

const val COMMENT_TYPE = "COMMENT_TYPE"

const val DATE_CODE = "DATE_CODE"

// Result Code

const val LOGIN_TO_MAIN_ACTIVITY = 0

// 给rv传值，判断布局需要wrap_content还是match_parent，不填则为默认
// 设置布局为MATCH_PARENT可以使rv在GridLayoutManager下能居中，反之不能

const val VIDEO_LAYOUT_WRAP_CONTENT = 1

const val VIDEO_LAYOUT_MATCH_PARENT = 2

// 給CommentFragment傳值，判斷是影片評論區還是預覽評論區 [COMMENT_TYPE]

const val VIDEO_COMMENT_PREFIX = "video"

const val PREVIEW_COMMENT_PREFIX = "preview"

// base url

const val HANIME_BASE_URL = "https://hanime1.me/"

const val HANIME_DOMAIN = "hanime1.me"

const val HANIME_LOGIN_URL = HANIME_BASE_URL + "login"

// for Shared Preference

const val LOGIN_COOKIE = "cookie"

const val ALREADY_LOGIN = "already_login"

// for My List

enum class MyListType(val value: String) {
    FAV_VIDEO("LL"),
    WATCH_LATER("WL")
}

enum class FavStatus(val value: String) {
    ADD_FAV(EMPTY_STRING),
    CANCEL_FAV("1")
}