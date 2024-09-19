package com.yenaly.han1meviewer.logic.model

import com.yenaly.han1meviewer.EMPTY_STRING

enum class MyListType(val value: String) {
    FAV_VIDEO("LL"),
    WATCH_LATER("WL"),
    SUBSCRIPTION("SL")
}

enum class FavStatus(val value: String) {
    ADD_FAV(EMPTY_STRING),
    CANCEL_FAV("1")
}

enum class CommentPlace(val value: String) {
    COMMENT("comment"), // 主評論
    CHILD_COMMENT("reply") // 子評論
}