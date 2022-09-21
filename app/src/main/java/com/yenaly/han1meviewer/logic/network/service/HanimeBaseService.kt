package com.yenaly.han1meviewer.logic.network.service

import androidx.annotation.IntRange
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:10
 */
interface HanimeBaseService {

    @GET("/")
    suspend fun getHomePage(): Response<ResponseBody>

    @GET("search")
    suspend fun getHanimeSearchResult(
        @Query("page") @IntRange(from = 1) page: Int = 1,
        @Query("query") query: String? = null,
        @Query("genre") genre: String? = null,
        @Query("sort") sort: String? = null,
        @Query("broad") broad: String? = null,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null,
        @Query("duration") duration: String? = null,
        @Query("tags[]") tags: LinkedHashSet<String> = linkedSetOf(),
        @Query("brands[]") brands: LinkedHashSet<String> = linkedSetOf()
    ): Response<ResponseBody>

    @GET("watch")
    suspend fun getHanimeVideo(
        @Query("v") videoCode: String
    ): Response<ResponseBody>

    @GET("previews/{date}")
    suspend fun getHanimePreview(
        @Path("date") date: String // 类似 202206. 202012
    ): Response<ResponseBody>

    @GET("playlist")
    suspend fun getMyList(
        @Query("page") @IntRange(from = 1) page: Int,
        @Query("list") listType: String // WL, LL
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("deletePlayitem")
    suspend fun deleteMyList(
        @Field("playlist_id") listType: String,
        @Field("video_id") videoCode: String,
        @Field("count") count: Int = 1, // 隨便傳一個就行
        @Header("X-CSRF-TOKEN") csrfToken: String?
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("like")
    suspend fun addToMyFavVideo(
        @Field("like-foreign-id") videoCode: String,
        @Field("like-status") likeStatus: String,
        @Field("_token") csrfToken: String?,
        @Field("like-user-id") userId: String?, // 這網站有點難綳，傳這種參數
        @Field("like-is-positive") likeIsPositive: Int = 1,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken
    ): Response<ResponseBody>
}