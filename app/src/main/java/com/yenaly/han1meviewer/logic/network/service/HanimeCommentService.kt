package com.yenaly.han1meviewer.logic.network.service

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/09/19 019 17:44
 */
interface HanimeCommentService {
    @GET("loadComment")
    suspend fun getComment(
        @Query("type") type: String, // 類似 "video", "preview"
        @Query("id") code: String
    ): Response<ResponseBody>

    @GET("loadReplies")
    suspend fun getCommentReply(
        @Query("id") commentId: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("createComment")
    suspend fun postComment(
        @Field("_token") csrfToken: String?,
        @Field("comment-user-id") currentUserId: String,
        @Field("comment-type") type: String, // 類似 "video", "preview"
        @Field("comment-foreign-id") targetUserId: String,
        @Field("comment-text") text: String,
        @Field("comment-count") count: Int = 1, // 感觉没什么用，仅前端用
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("replyComment")
    suspend fun postCommentReply(
        @Field("_token") csrfToken: String?,
        @Field("reply-comment-id") replyCommentId: String,
        @Field("reply-comment-text") text: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken
    ): Response<ResponseBody>
}