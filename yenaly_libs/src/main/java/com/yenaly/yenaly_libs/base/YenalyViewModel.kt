package com.yenaly.yenaly_libs.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yenaly.yenaly_libs.utils.unsafeLazy

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/20 020 11:37
 * @Description : Description...
 */
open class YenalyViewModel(
    @JvmField protected val application: Application
) : AndroidViewModel(application) {

    var parent: YenalyViewModel? = null
        private set

    @Suppress("UNCHECKED_CAST")
    fun <YVM : YenalyViewModel> parent(): YVM? = parent as? YVM

    fun <YVM : YenalyViewModel> requireParent(): YVM = parent() ?: error("Parent not found")

    inline fun <reified YVM : YenalyViewModel> sub() = sub(YVM::class.java)

    fun <YVM : YenalyViewModel> sub(clazz: Class<YVM>): Lazy<YVM> = unsafeLazy {
        clazz.getConstructor(Application::class.java).newInstance(application).also {
            it.parent = this
        }
    }
}