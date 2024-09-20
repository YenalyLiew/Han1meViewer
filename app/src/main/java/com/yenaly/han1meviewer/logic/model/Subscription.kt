package com.yenaly.han1meviewer.logic.model

/**
 * @since 2024/09/11
 */
data class Subscription(
    val name: String,
    val avatarUrl: String?,
    val artistId: String?,
    // for adapter
    val isCheckBoxVisible: Boolean = false,
    val isDeleteVisible: Boolean = false,
)
