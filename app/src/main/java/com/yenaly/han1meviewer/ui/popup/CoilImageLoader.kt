package com.yenaly.han1meviewer.ui.popup

import android.content.Context
import android.graphics.Matrix
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.interfaces.XPopupImageLoader
import com.lxj.xpopup.photoview.PhotoView
import java.io.File

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/26 026 22:00
 */
class CoilImageLoader(@DrawableRes private val errImgRes: Int = 0) : XPopupImageLoader {

    override fun loadSnapshot(uri: Any, snapshot: PhotoView, srcView: ImageView?) {
        snapshot.load(uri)
    }

    override fun loadImage(
        position: Int,
        uri: Any,
        popupView: ImageViewerPopupView,
        snapshot: PhotoView,
        progressBar: ProgressBar,
    ): View {
        progressBar.isVisible = true
        val photoView = PhotoView(popupView.context).apply {
            isZoomable = false
            setOnMatrixChangeListener {
                val matrix = Matrix()
                this.getSuppMatrix(matrix)
                snapshot.setSuppMatrix(matrix)
            }
            this.setOnClickListener {
                popupView.dismiss()
            }
            popupView.longPressListener?.let {
                this.setOnLongClickListener {
                    popupView.longPressListener.onLongPressed(popupView, position)
                    return@setOnLongClickListener false
                }
            }
        }

        if (snapshot.drawable != null && (snapshot.tag as Int) == position) {
            photoView.setImageDrawable(snapshot.drawable.constantState?.newDrawable())
        }
        photoView.load(uri) {
            error(errImgRes)
            listener(object : ImageRequest.Listener {
                override fun onError(request: ImageRequest, result: ErrorResult) {
                    progressBar.isVisible = false
                    photoView.isZoomable = false
                }

                override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                    progressBar.isVisible = false
                    photoView.isZoomable = true
                }
            })
        }

        return photoView
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun getImageFile(context: Context, uri: Any): File? {
        return context.imageLoader.diskCache?.openSnapshot(uri.toString())?.use { snapshot ->
            snapshot.data.toFile()
        }
    }
}