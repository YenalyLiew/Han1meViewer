package com.yenaly.han1meviewer

/**
 * 这个注解提示大家不要随便用这个变量或者方法，
 * 用在别处可能会引起空指针或者未初始化异常！
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/10 010 19:50
 */
annotation class UsingCautiously(
    val message: String = EMPTY_STRING
)
