package com.yenaly.han1meviewer.logic.model

import android.os.Parcelable
import android.util.SparseArray
import androidx.core.util.valueIterator
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.LanguageHelper
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@Suppress("EqualsOrHashCode")
@Serializable
@Parcelize
data class SearchOption(
    @SerialName("lang")
    val lang: Language? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("search_key")
    val searchKey: String? = null,
) : Parcelable {

    companion object {
        fun SparseArray<Set<SearchOption>>.flatten(): Set<String> = buildSet {
            valueIterator().forEach { options ->
                val res = options.mapNotNullTo(mutableSetOf()) { it.searchKey }
                addAll(res)
            }
        }

        operator fun Map<String, List<SearchOption>>.get(scopeNameRes: Int): List<SearchOption> {
            return when (scopeNameRes) {
                R.string.video_attr -> this["video_attributes"].orEmpty()
                R.string.relationship -> this["character_relationships"].orEmpty()
                R.string.characteristics -> this["characteristics"].orEmpty()
                R.string.appearance_and_figure -> this["appearance_and_figure"].orEmpty()
                R.string.story_plot -> this["story_plot"].orEmpty()
                R.string.sex_position -> this["sex_positions"].orEmpty()
                else -> error("Unknown scope name res: $scopeNameRes")
            }
        }

        fun toScopeKey(raw: String): Int = when (raw) {
            "video_attributes" -> R.string.video_attr
            "character_relationships" -> R.string.relationship
            "characteristics" -> R.string.characteristics
            "appearance_and_figure" -> R.string.appearance_and_figure
            "story_plot" -> R.string.story_plot
            "sex_positions" -> R.string.sex_position
            else -> error("Unknown scope name: $raw")
        }
    }

    @Serializable
    @Parcelize
    data class Language(
        @SerialName("zh-rCN")
        val zhrCN: String? = null,
        @SerialName("zh-rTW")
        val zhrTW: String? = null,
        @SerialName("en")
        val en: String? = null,
    ) : Parcelable

    override fun hashCode(): Int = searchKey.hashCode()

    val value: String
        get() = when {
            lang == null -> name.orEmpty()
            name == null -> LanguageHelper.preferredLanguage.let { pl ->
                when (pl.language) {
                    Locale.CHINESE.language -> when (pl.country) {
                        Locale.SIMPLIFIED_CHINESE.country -> lang.zhrCN
                        else -> lang.zhrTW
                    }

                    Locale.ENGLISH.language -> lang.en
                    else -> lang.zhrTW
                }
            } ?: lang.zhrTW.orEmpty()

            else -> throw IllegalArgumentException("Unknown lang type: ${lang.javaClass.name}")
        }
}