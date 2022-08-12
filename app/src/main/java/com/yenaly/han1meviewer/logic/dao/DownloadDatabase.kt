package com.yenaly.han1meviewer.logic.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadedEntity
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/07 007 18:26
 */
@Database(
    entities = [HanimeDownloadedEntity::class],
    version = 1, exportSchema = false
)
abstract class DownloadDatabase : RoomDatabase() {

    abstract val hanimeDownloadedDao: HanimeDownloadedDao

    companion object {
        val instance by lazy {
            Room.databaseBuilder(
                applicationContext,
                DownloadDatabase::class.java,
                "download.db"
            ).build()
        }
    }
}