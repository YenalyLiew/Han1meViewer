package com.yenaly.yenaly_libs.utils

import android.provider.Settings
import android.view.OrientationEventListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/04/10 010 20:49
 */
class OrientationManager(private var orientationChangeListener: OrientationChangeListener? = null) :
    OrientationEventListener(applicationContext), LifecycleEventObserver {

    private var screenOrientation: ScreenOrientation = ScreenOrientation.PORTRAIT

    enum class ScreenOrientation {
        LANDSCAPE, REVERSED_LANDSCAPE,
        PORTRAIT, REVERSED_PORTRAIT;

        val isPortrait get() = this == PORTRAIT || this == REVERSED_PORTRAIT
        val isLandscape get() = this == LANDSCAPE || this == REVERSED_LANDSCAPE
    }

    override fun onOrientationChanged(orientation: Int) {
        if (orientation == -1) {
            return
        }
        try {
            val isRotateEnabled = Settings.System.getInt(
                applicationContext.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION
            )
            if (isRotateEnabled == 0) return
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        val newOrientation = when (orientation) {
            in 60..140 -> ScreenOrientation.REVERSED_LANDSCAPE
            in 140..220 -> ScreenOrientation.REVERSED_PORTRAIT
            in 220..300 -> ScreenOrientation.LANDSCAPE
            else -> ScreenOrientation.PORTRAIT
        }
        if (newOrientation !== screenOrientation) {
            screenOrientation = newOrientation
            orientationChangeListener?.onOrientationChanged(screenOrientation)
        }
    }

    fun interface OrientationChangeListener {
        fun onOrientationChanged(orientation: ScreenOrientation)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> enable()
            Lifecycle.Event.ON_STOP -> disable()
            else -> Unit
        }
    }
}