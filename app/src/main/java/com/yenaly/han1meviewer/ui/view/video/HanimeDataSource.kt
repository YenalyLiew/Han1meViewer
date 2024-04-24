package com.yenaly.han1meviewer.ui.view.video

import cn.jzvd.JZDataSource
import com.yenaly.han1meviewer.ResolutionLinkMap

class HanimeDataSource : JZDataSource {

    private val urlsList = mutableListOf<Map.Entry<Any?, Any?>>()

    @Suppress("UNCHECKED_CAST")
    constructor(title: String, resolutionLinkMap: ResolutionLinkMap) : this() {
        this.currentUrlIndex = 0
        urlsList.clear()
        this.urlsMap.also { map ->
            map.clear()
            resolutionLinkMap.mapValuesTo(map) { it.value.link }
            urlsList.addAll(map.entries as Set<Map.Entry<Any?, Any?>>)
        }
        this.title = title
        this.headerMap = hashMapOf()
        this.looping = false
        this.objects = null
    }

    override fun getKeyFromDataSource(index: Int): String? {
        return urlsList.getOrNull(index)?.key?.toString()
    }

    override fun getValueFromLinkedMap(index: Int): Any? {
        return urlsList.getOrNull(index)?.value
    }

    private constructor() : super("")


}