package com.yenaly.han1meviewer.logic.model

import com.google.gson.annotations.SerializedName

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/09/09 009 21:24
 */
data class VersionModel(
    val url: String,

    @SerializedName("assets_url")
    val assetsURL: String,

    @SerializedName("upload_url")
    val uploadURL: String,

    @SerializedName("html_url")
    val htmlURL: String,

    val id: Long,
    val author: Author,

    @SerializedName("node_id")
    val nodeID: String,

    @SerializedName("tag_name")
    val tagName: String,

    @SerializedName("target_commitish")
    val targetCommitish: String,

    val name: String,
    val draft: Boolean,
    val prerelease: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("published_at")
    val publishedAt: String,

    val assets: List<Asset>,

    @SerializedName("tarball_url")
    val tarballURL: String,

    @SerializedName("zipball_url")
    val zipballURL: String,

    val body: String
) {
    data class Asset (
        val url: String,
        val id: Long,

        @SerializedName("node_id")
        val nodeID: String,

        val name: String,
        val label: Any? = null,
        val uploader: Author,

        @SerializedName("content_type")
        val contentType: String,

        val state: String,
        val size: Long,

        @SerializedName("download_count")
        val downloadCount: Long,

        @SerializedName("created_at")
        val createdAt: String,

        @SerializedName("updated_at")
        val updatedAt: String,

        @SerializedName("browser_download_url")
        val browserDownloadURL: String
    )

    data class Author (
        val login: String,
        val id: Long,

        @SerializedName("node_id")
        val nodeID: String,

        @SerializedName("avatar_url")
        val avatarURL: String,

        @SerializedName("gravatar_id")
        val gravatarID: String,

        val url: String,

        @SerializedName("html_url")
        val htmlURL: String,

        @SerializedName("followers_url")
        val followersURL: String,

        @SerializedName("following_url")
        val followingURL: String,

        @SerializedName("gists_url")
        val gistsURL: String,

        @SerializedName("starred_url")
        val starredURL: String,

        @SerializedName("subscriptions_url")
        val subscriptionsURL: String,

        @SerializedName("organizations_url")
        val organizationsURL: String,

        @SerializedName("repos_url")
        val reposURL: String,

        @SerializedName("events_url")
        val eventsURL: String,

        @SerializedName("received_events_url")
        val receivedEventsURL: String,

        val type: String,

        @SerializedName("site_admin")
        val siteAdmin: Boolean
    )
}


