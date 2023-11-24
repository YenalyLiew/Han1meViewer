package com.yenaly.han1meviewer

import com.yenaly.yenaly_libs.utils.appScreenWidth
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * 我觉得空字符串写出来太逆天了，所以搞了个常量
 */
const val EMPTY_STRING = ""

// 标准时间格式

const val DATE_FORMAT = "yyyy-MM-dd"

const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"

// 网络基本设置

const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36"

// 動態設置影片卡片長寬

val VIDEO_IN_ONE_LINE
    get() =
        (appScreenWidth / applicationContext.resources.getDimension(R.dimen.video_cover_width)).toInt()

val SIMPLIFIED_VIDEO_IN_ONE_LINE
    get() =
        (appScreenWidth / applicationContext.resources.getDimension(R.dimen.video_cover_simplified_width)).toInt()

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

@Deprecated("Use [ADVANCED_SEARCH_MAP] instead")
const val FROM_VIDEO_TAG = "FROM_VIDEO_TAG"

/**
 * 接受类型 [AdvancedSearchMap] 或者 [String]
 */
const val ADVANCED_SEARCH_MAP = "ADVANCED_SEARCH_MAP"

const val COMMENT_ID = "COMMENT_ID"

const val COMMENT_TYPE = "COMMENT_TYPE"

const val DATE_CODE = "DATE_CODE"

const val CSRF_TOKEN = "CSRF_TOKEN"

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

const val HANIME_HOSTNAME = "hanime1.me"

const val HANIME_BASE_URL = "https://hanime1.me/"

const val HANIME_LOGIN_URL = HANIME_BASE_URL + "login"

// github url

const val HA1_GITHUB_URL = "https://github.com/YenalyLiew/Han1meViewer/"

const val HA1_GITHUB_ISSUE_URL = HA1_GITHUB_URL + "issues"

const val HA1_GITHUB_FORUM_URL = HA1_GITHUB_URL + "discussions"

// for Shared Preference

const val LOGIN_COOKIE = "cookie"

const val ALREADY_LOGIN = "already_login"

// Notification

const val DOWNLOAD_NOTIFICATION_CHANNEL = "download_channel"