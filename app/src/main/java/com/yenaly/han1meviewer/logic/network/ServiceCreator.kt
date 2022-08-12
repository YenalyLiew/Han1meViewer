package com.yenaly.han1meviewer.logic.network

import com.yenaly.han1meviewer.USER_AGENT
import com.yenaly.han1meviewer.cookieMap
import com.yenaly.han1meviewer.loginCookie
import com.yenaly.han1meviewer.toCookieList
import okhttp3.*
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

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

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
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
        .build()
}