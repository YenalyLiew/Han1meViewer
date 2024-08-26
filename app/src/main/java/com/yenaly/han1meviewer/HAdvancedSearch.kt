package com.yenaly.han1meviewer

import java.io.Serializable

/**
 * 高级搜索的枚举
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/09/22 022 18:13
 */
enum class HAdvancedSearch {
    /**
     * 搜索关键词，类型 [String]
     */
    QUERY,

    /**
     * 影片类型，类型 [String]
     */
    GENRE,

    /**
     * 排序方式，类型 [String]
     */
    SORT,

    /**
     * 影片年份，类型 [Int]
     */
    YEAR,

    /**
     * 影片月份，类型 [Int]
     */
    MONTH,

    /**
     * 影片时长，类型 [String]
     */
    DURATION,

    /**
     * 影片 tag，类型 [String] (Deprecated) 或 HashSet&lt;String&gt; (Deprecated)
     * 或 Map<Int, String> 或 Map<Int, HashSet&lt;String&gt;>
     */
    TAGS,

    /**
     * 影片品牌，类型 [String] 或 [HashSet&lt;String&gt;]
     */
    BRANDS
}

/**
 * 高级搜索的 Map，所有给 SearchActivity 的传参走这里！
 */
typealias AdvancedSearchMap = HashMap<HAdvancedSearch, Serializable>

@Suppress("NOTHING_TO_INLINE")
inline fun advancedSearchMapOf(vararg pairs: Pair<HAdvancedSearch, Serializable>) =
    hashMapOf(*pairs)