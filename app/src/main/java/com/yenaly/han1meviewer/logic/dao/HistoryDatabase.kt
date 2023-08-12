package com.yenaly.han1meviewer.logic.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/22 022 22:46
 */
@Database(
    entities = [SearchHistoryEntity::class, WatchHistoryEntity::class],
    version = 1, exportSchema = false
)
abstract class HistoryDatabase : RoomDatabase() {

    abstract val searchHistory: SearchHistoryDao

    abstract val watchHistory: WatchHistoryDao

    companion object {
        val instance by lazy {
            Room.databaseBuilder(
                applicationContext,
                HistoryDatabase::class.java,
                "history.db"
            ).build()
        }
    }
}



