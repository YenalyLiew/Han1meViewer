@file:Suppress("PLUGIN_IS_NOT_ENABLED")

package com.yenaly.han1meviewer.logic.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.yenaly.han1meviewer.logic.model.MultiItemEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 */
interface HKeyframeType : MultiItemEntity {
    companion object {
        const val H_KEYFRAME = 0
        const val HEADER = 1
    }
}

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/12 012 12:14
 */
@Serializable
@Entity
@TypeConverters(HKeyframeEntity.KeyframeTypeConverter::class)
data class HKeyframeEntity(
    @PrimaryKey val videoCode: String,
    val title: String,
    /**
     * 关键帧列表
     */
    val keyframes: MutableList<Keyframe>,
    /**
     * 最后修改时间
     */
    val lastModifiedTime: Long = -1,
    /**
     * 创建时间
     */
    val createdTime: Long = -1,
    /**
     * 作者，null 代表本地（自己创建的），非 null 代表共享
     */
    val author: String? = null,
) : HKeyframeType {

    /**
     * 分组，一个系列为一组
     */
    @Ignore
    val group: String? = null

    /**
     * 所在集数
     */
    @Ignore
    val episode: Int = -1

    @Ignore
    override val itemType: Int = HKeyframeType.H_KEYFRAME

    @Serializable
    data class Keyframe(
        /**
         * 该关键帧的时间戳
         */
        val position: Long,
        /**
         * 该关键帧的提示
         */
        val prompt: String?,
    )

    class KeyframeTypeConverter {
        @TypeConverter
        fun fromKeyframeList(keyframes: MutableList<Keyframe>): String =
            Json.encodeToString(keyframes)

        @TypeConverter
        fun toKeyframeList(keyframes: String): MutableList<Keyframe> =
            Json.decodeFromString(keyframes)
    }
}

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 */
data class HKeyframeHeader(
    val title: String,
    /**
     * 附属的关键帧列表
     */
    val attached: List<HKeyframeEntity>,
    override val itemType: Int = HKeyframeType.HEADER,
) : HKeyframeType