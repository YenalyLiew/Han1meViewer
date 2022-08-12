package com.yenaly.han1meviewer

/**
 * 为什么有这个注解，因为有些地方我处理不好（比如某个变量只能在调用某函数之后使用），
 * 我只能用这个注解提示大家不要随便用这个变量或者方法，
 * 用在别处可能会引起空指针或者未初始化异常！
 *
 * 我肯定是希望没有任何变量或者方法加上这个注解，看自己以后的造化了。
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/10 010 19:50
 */
annotation class UsingCautiously(
    val message: String = ""
)
