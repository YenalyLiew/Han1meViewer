package com.yenaly.yenaly_libs

/**
 * @author Yenaly Liew
 * @time 2023/08/29 029 13:58
 */
abstract class SingleArgSingletonHolder<out T, in A>(private var constructor: ((A) -> T)?) {
    @Volatile
    private var instance: T? = null
    fun getInstance(arg: A): T = instance ?: synchronized(this) {
        instance ?: constructor!!(arg).also {
            instance = it
            constructor = null
        }
    }
}