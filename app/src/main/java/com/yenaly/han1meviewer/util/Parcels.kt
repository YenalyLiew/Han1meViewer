package com.yenaly.han1meviewer.util

import android.os.Parcel

@Suppress("NOTHING_TO_INLINE")
inline fun Parcel.readBooleanCompat(): Boolean {
    return readInt() != 0
}

@Suppress("NOTHING_TO_INLINE")
inline fun Parcel.writeBooleanCompat(value: Boolean) {
    writeInt(if (value) 1 else 0)
}