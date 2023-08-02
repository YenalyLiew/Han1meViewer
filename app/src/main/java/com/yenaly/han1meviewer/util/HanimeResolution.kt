package com.yenaly.han1meviewer.util

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/10/11 011 21:19
 */
class HanimeResolution {

    private val resArray = arrayOfNulls<Pair<String, String>>(5)

    companion object {

        // 目前hanime1有的分辨率好像就這些，暫時不考慮其他分辨率

        const val RES_1080P = "1080P"
        const val RES_720P = "720P"
        const val RES_480P = "480P"
        const val RES_240P = "240P"
        const val RES_UNKNOWN = "Unknown P"
    }

    /**
     * 解析分辨率，從高到低排列。
     *
     * @param resString 分辨率
     * @param resLink 分辨率對應網址
     */
    fun parseResolution(resString: String?, resLink: String) {
        when (resString) {
            RES_1080P -> resArray[0] = RES_1080P to resLink
            RES_720P -> resArray[1] = RES_720P to resLink
            RES_480P -> resArray[2] = RES_480P to resLink
            RES_240P -> resArray[3] = RES_240P to resLink
            null -> resArray[4] = RES_UNKNOWN to resLink
        }
    }

    fun toLinkedHashMap(): LinkedHashMap<String, String> {
        val linkedHashMap = linkedMapOf<String, String>()
        resArray.forEach {
            if (it != null) {
                linkedHashMap[it.first] = it.second
            }
        }
        return linkedHashMap
    }
}