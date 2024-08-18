package com.yenaly.han1meviewer.logic.network.service

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface HanimeSubscriptionService {

    @FormUrlEncoded
    @POST("subscribe")
    suspend fun subscribeArtist(
        @Field("_token") csrfToken: String?,
        @Field("subscribe-user-id") userId: String,
        @Field("subscribe-artist-id") artistId: String,
        // 如果当前未订阅会发送空字符串，否则发1
        @Field("subscribe-status") status: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): Response<ResponseBody>
}