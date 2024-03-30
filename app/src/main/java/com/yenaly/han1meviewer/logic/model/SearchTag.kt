package com.yenaly.han1meviewer.logic.model

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/09 009 21:27
 */
data class SearchTag(
    val genres: List<String>,
    val tags: Map<String, List<String>>,
    val sortOptions: List<String>,
    val brands: List<String>,
    val releaseDates: ReleaseDate,
    val durationOptions: List<Pair<String, String>>,
) {
    data class ReleaseDate(
        val years: List<Pair<String, String>>,
        val months: List<Pair<String, String>>,
    )
}