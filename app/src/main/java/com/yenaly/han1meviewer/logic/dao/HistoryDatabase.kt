package com.yenaly.han1meviewer.logic.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity

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
        private var historyDatabase: HistoryDatabase? = null
        fun getInstance(context: Context): HistoryDatabase {
            return historyDatabase ?: synchronized(HistoryDatabase::class.java) {
                historyDatabase = Room.databaseBuilder(
                    context,
                    HistoryDatabase::class.java,
                    "history.db"
                ).build()
                historyDatabase!!
            }
        }
    }
}



