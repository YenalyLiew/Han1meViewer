package com.yenaly.yenaly_libs.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.res.Configuration
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService

object DeviceUtil {
    val isTablet: Boolean
        get() = applicationContext.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

    val isBluetoothConnected: Boolean
        @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT) get() {
            val bm = applicationContext.getSystemService<BluetoothManager>() ?: return false
            val adapter = bm.adapter
            if (adapter != null && adapter.isEnabled) {
                val pairedDevices = adapter.bondedDevices
                return pairedDevices.any { device ->
                    bm.getConnectionState(
                        device, BluetoothProfile.STATE_CONNECTED
                    ) == BluetoothProfile.STATE_CONNECTED
                }
            }
            return false
        }

    val isHeadsetConnected: Boolean
        @SuppressLint("ObsoleteSdkInt") get() {
            val am = applicationContext.getSystemService<AudioManager>() ?: return false
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            return devices.any { device ->
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && device.type == AudioDeviceInfo.TYPE_USB_HEADSET)
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && device.type == AudioDeviceInfo.TYPE_BLE_HEADSET)
            }
        }
}
