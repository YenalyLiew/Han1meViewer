package com.yenaly.han1meviewer.logic.network.interceptor

import com.yenaly.yenaly_libs.utils.unsafeLazy
import okhttp3.ResponseBody
import okio.Throttler
import okio.buffer

class SpeedLimitResponseBody(
    private val responseBody: ResponseBody,
    /**
     * 0 means no limit
     */
    private val maxSpeed: Long
) : ResponseBody() {

    private val throttler by unsafeLazy {
        Throttler().apply { bytesPerSecond(maxSpeed) }
    }

    override fun contentLength(): Long = responseBody.contentLength()

    override fun contentType() = responseBody.contentType()

    override fun source() = throttler.source(responseBody.source()).buffer()
}