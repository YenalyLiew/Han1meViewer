package com.yenaly.han1meviewer.logic.network.service

import androidx.annotation.IntRange
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * MyList 是指 喜欢的影片 + 稍后再看
 *
 * Playlist 是指 自定义的播放列表
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/26 026 16:30
 */
interface HanimeMyListService {
    @GET("playlist")
    suspend fun getMyListItems(
        @Query("page") @IntRange(from = 1) page: Int,
        @Query("list") typeOrCode: String, // WL, LL, SL, 12534..
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("deletePlayitem")
    suspend fun deleteMyListItems(
        @Field("playlist_id") listType: String,
        @Field("video_id") videoCode: String,
        @Field("count") count: Int = 1, // 隨便傳一個就行
        @Header("X-CSRF-TOKEN") csrfToken: String?,
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("like")
    suspend fun addToMyFavVideo(
        @Field("like-foreign-id") videoCode: String,
        @Field("like-status") likeStatus: String,
        @Field("_token") csrfToken: String?,
        @Field("like-user-id") userId: String?,
        @Field("like-is-positive") isPositive: Int = 1,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): Response<ResponseBody>

    @GET("playlists")
    suspend fun getPlaylists(): Response<ResponseBody>

    @FormUrlEncoded
    @POST("createPlaylist")
    suspend fun createPlaylist(
        @Field("_token") csrfToken: String?,
        @Field("create-playlist-video-id") videoCode: String,
        @Field("playlist-title") title: String,
        @Field("playlist-description") description: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("save")
    suspend fun addToMyList(
        @Field("_token") csrfToken: String?,
        @Field("input_id") listCode: String,
        @Field("video_id") videoCode: String,
        @Field("is_checked") isChecked: Boolean,
        @Field("user_id") userId: String = "",
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("playlist/{list_code}")
    suspend fun modifyPlaylist(
        @Path("list_code") listCode: String,
        @Field("playlist-title") title: String,
        @Field("playlist-description") description: String,
        @Field("playlist-delete") delete: String?, // 删除 "on"，不删除 null
        @Field("_token") csrfToken: String?,
        @Field("_method") method: String? = "PUT",
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): Response<ResponseBody>
}