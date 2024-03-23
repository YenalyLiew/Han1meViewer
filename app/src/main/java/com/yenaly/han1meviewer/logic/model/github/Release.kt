package com.yenaly.han1meviewer.logic.model.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/09/09 009 21:24
 */
@Serializable
data class Release(
    val url: String,

    @SerialName("assets_url")
    val assetsURL: String,

    @SerialName("upload_url")
    val uploadURL: String,

    @SerialName("html_url")
    val htmlURL: String,

    val id: Long,
    val author: Author,

    @SerialName("node_id")
    val nodeID: String,

    @SerialName("tag_name")
    val tagName: String,

    @SerialName("target_commitish")
    val targetCommitish: String,

    val name: String,
    val draft: Boolean,
    val prerelease: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("published_at")
    val publishedAt: String,

    val assets: List<Asset>,

    @SerialName("tarball_url")
    val tarballURL: String,

    @SerialName("zipball_url")
    val zipballURL: String,

    val body: String,
) {
    @Serializable
    data class Asset(
        val url: String,
        val id: Long,

        @SerialName("node_id")
        val nodeID: String,

        val name: String,
        val label: String? = null,
        val uploader: Author,

        @SerialName("content_type")
        val contentType: String,

        val state: String,
        val size: Long,

        @SerialName("download_count")
        val downloadCount: Long,

        @SerialName("created_at")
        val createdAt: String,

        @SerialName("updated_at")
        val updatedAt: String,

        @SerialName("browser_download_url")
        val browserDownloadURL: String,
    )

    @Serializable
    data class Author(
        val login: String,
        val id: Long,

        @SerialName("node_id")
        val nodeID: String,

        @SerialName("avatar_url")
        val avatarURL: String,

        @SerialName("gravatar_id")
        val gravatarID: String,

        val url: String,

        @SerialName("html_url")
        val htmlURL: String,

        @SerialName("followers_url")
        val followersURL: String,

        @SerialName("following_url")
        val followingURL: String,

        @SerialName("gists_url")
        val gistsURL: String,

        @SerialName("starred_url")
        val starredURL: String,

        @SerialName("subscriptions_url")
        val subscriptionsURL: String,

        @SerialName("organizations_url")
        val organizationsURL: String,

        @SerialName("repos_url")
        val reposURL: String,

        @SerialName("events_url")
        val eventsURL: String,

        @SerialName("received_events_url")
        val receivedEventsURL: String,

        val type: String,

        @SerialName("site_admin")
        val siteAdmin: Boolean,
    )
}


