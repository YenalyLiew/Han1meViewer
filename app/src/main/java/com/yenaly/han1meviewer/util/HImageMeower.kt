package com.yenaly.han1meviewer.util

import android.util.Log
import android.widget.ImageView
import coil.ImageLoader
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import com.yenaly.yenaly_libs.utils.applicationContext
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

@Suppress("NOTHING_TO_INLINE")
object HImageMeower {

    private const val TAG = "CoilImageNyanner"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    private val imageLoader = ImageLoader.Builder(applicationContext)
        .okHttpClient(okHttpClient)
        .build()

    suspend fun execute(data: Any): ImageResult {
        Log.d(TAG, "execute: $data")
        return imageLoader.execute(
            ImageRequest.Builder(applicationContext).data(data).build()
        )
    }

    inline fun placeholder(height: Int, width: Int, blur: Int = 8) =
        "https://picsum.photos/$width/$height/?blur=$blur"

    fun ImageView.loadUnhappily(data: Any?, fallbackData: Any?) {
        Log.d(TAG, "primary: $data, fallback: $fallbackData")
        val primaryRequest = ImageRequest.Builder(context)
            .data(data)
            .crossfade(true)
            .target(this)
            .listener(object : ImageRequest.Listener {
                private val ivRef = WeakReference(this@loadUnhappily)
                override fun onError(request: ImageRequest, result: ErrorResult) {
                    fallbackData?.let { ivRef.get()?.loadUnhappily(it, null) }
                }
            }).build()
        context.imageLoader.enqueue(primaryRequest)
    }
}