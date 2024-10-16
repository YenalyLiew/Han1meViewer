package com.yenaly.yenaly_libs.utils

/**
 * Android一般用不到[LazyThreadSafetyMode.SYNCHRONIZED]的lazy，
 * 使用[LazyThreadSafetyMode.NONE]更合适。
 */
fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)