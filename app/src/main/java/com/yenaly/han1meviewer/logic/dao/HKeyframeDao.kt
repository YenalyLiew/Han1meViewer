package com.yenaly.han1meviewer.logic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
import kotlinx.coroutines.flow.Flow

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/12 012 12:39
 */
@Dao
abstract class HKeyframeDao {

    @Query("SELECT * FROM HKeyframeEntity ORDER BY createdTime DESC")
    abstract fun loadAll(): Flow<MutableList<HKeyframeEntity>>

    @Query("SELECT * FROM HKeyframeEntity WHERE `title` LIKE '%' || :keyword || '%' OR `videoCode` == :keyword ORDER BY createdTime DESC")
    abstract fun loadAll(keyword: String): Flow<MutableList<HKeyframeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: HKeyframeEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(entity: HKeyframeEntity)

    @Delete
    abstract suspend fun delete(entity: HKeyframeEntity)

    @Query("SELECT * FROM HKeyframeEntity WHERE `videoCode` == :videoCode LIMIT 1")
    abstract suspend fun findBy(videoCode: String): HKeyframeEntity?

    @Query("SELECT * FROM HKeyframeEntity WHERE `videoCode` == :videoCode LIMIT 1")
    abstract fun observe(videoCode: String): Flow<HKeyframeEntity?>

    open suspend fun modifyKeyframe(
        videoCode: String,
        oldKeyframe: HKeyframeEntity.Keyframe, keyframe: HKeyframeEntity.Keyframe,
    ) {
        val entity = findBy(videoCode)
        entity?.let {
            // 按理説一定會有，如果沒有那就是出問題了
            if (keyframe == oldKeyframe) return
            removeKeyframe(videoCode, oldKeyframe)
            appendKeyframe(videoCode, entity.title, keyframe)
        }
    }

    open suspend fun appendKeyframe(
        videoCode: String, title: String,
        keyframe: HKeyframeEntity.Keyframe,
    ) {
        val entity = findBy(videoCode)
        if (entity == null) {
            insert(
                HKeyframeEntity(
                    videoCode,
                    title,
                    mutableListOf(keyframe),
                    lastModifiedTime = System.currentTimeMillis(),
                    createdTime = System.currentTimeMillis(),
                    author = null
                )
            )
        } else {
            entity.keyframes += keyframe
            entity.keyframes.sortBy { it.position }
            update(entity.copy(lastModifiedTime = System.currentTimeMillis()))
        }
    }

    open suspend fun removeKeyframe(
        videoCode: String,
        keyframe: HKeyframeEntity.Keyframe,
    ) {
        val entity = findBy(videoCode)
        if (entity != null) {
            entity.keyframes -= keyframe
            if (entity.keyframes.isEmpty()) {
                delete(entity)
                return
            }
            update(entity.copy(lastModifiedTime = System.currentTimeMillis()))
        }
    }
}