package com.yenaly.han1meviewer.logic.model

import com.yenaly.han1meviewer.EMPTY_STRING

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/06 006 18:59
 */

enum class MyListType(val value: String) {
    FAV_VIDEO("LL"),
    WATCH_LATER("WL")
}

enum class FavStatus(val value: String) {
    ADD_FAV(EMPTY_STRING),
    CANCEL_FAV("1")
}

enum class CommentPlace(val value: String) {
    COMMENT("comment"), // 主評論
    CHILD_COMMENT("reply") // 子評論
}