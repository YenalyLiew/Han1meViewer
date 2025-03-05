package com.yenaly.han1meviewer.logic.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class SpeedLimitInterceptor(var maxSpeed: Long) : Interceptor {

    companion object {
        const val NO_LIMIT = 0L

        const val NO_LIMIT_INDEX = 0

        @JvmField
        val SPEED_BYTES = longArrayOf(
            /* 不限速 */ 0L,
            128 * 1024L, 256 * 1024L, 512 * 1024L,
            1024 * 1024L, 2048 * 1024L, 4096 * 1024L,
            8192 * 1024L, 10240 * 1024L
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return response.newBuilder()
            .body(response.body?.let {
                SpeedLimitResponseBody(it, maxSpeed)
            }).build()
    }
}