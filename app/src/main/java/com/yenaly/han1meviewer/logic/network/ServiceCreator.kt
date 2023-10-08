package com.yenaly.han1meviewer.logic.network

import com.yenaly.han1meviewer.HProxySelector
import com.yenaly.han1meviewer.USER_AGENT
import com.yenaly.han1meviewer.cookieMap
import com.yenaly.han1meviewer.loginCookie
import com.yenaly.han1meviewer.util.toCookieList
import com.yenaly.yenaly_libs.utils.GsonUtil
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:35
 */
object ServiceCreator {
    inline fun <reified T> create(baseUrl: String): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()
        .create(T::class.java)

    inline fun <reified T> createVersion(): T = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonUtil.gson))
        .build()
        .create(T::class.java)

    /**
     * OkHttpClient
     */
    var okHttpClient: OkHttpClient = buildOkHttpClient()
        private set

    /**
     * Rebuild OkHttpClient
     */
    fun rebuildOkHttpClient() {
        okHttpClient = buildOkHttpClient()
    }

    /**
     * Build OkHttpClient
     */
    private fun buildOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request =
                    chain.request().newBuilder().addHeader("User-Agent", USER_AGENT).build()
                return@addInterceptor chain.proceed(request)
            }
            .cookieJar(object : CookieJar {
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieMap?.get(url.host) ?: loginCookie.toCookieList(url.host)
                }

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieMap?.put(url.host, cookies)
                }
            })
            .proxySelector(HProxySelector())
            .build()
    }

    /**
     * Suspend extension that allows suspend [Call] inside coroutine.
     */
    suspend fun Call.await(): okhttp3.Response {
        return suspendCancellableCoroutine { continuation ->
            enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    continuation.resume(response)
                }
            })
            continuation.invokeOnCancellation { cancel() }
        }
    }
}