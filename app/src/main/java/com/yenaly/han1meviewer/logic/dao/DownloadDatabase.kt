package com.yenaly.han1meviewer.logic.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/07 007 18:26
 */
@Database(
    entities = [HanimeDownloadEntity::class],
    version = 2, exportSchema = false
)
abstract class DownloadDatabase : RoomDatabase() {

    abstract val hanimeDownloadDao: HanimeDownloadDao

    companion object {
        val instance by lazy {
            Room.databaseBuilder(
                applicationContext,
                DownloadDatabase::class.java,
                "download.db"
            ).addMigrations(migration_1_2).build()
        }

        private val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `HanimeDownloadEntity`(
                    `coverUrl` TEXT NOT NULL, `title` TEXT NOT NULL,
                    `addDate` INTEGER NOT NULL, `videoCode` TEXT NOT NULL,
                    `videoUri` TEXT NOT NULL, `quality` TEXT NOT NULL,
                    `videoUrl` TEXT NOT NULL, `length` INTEGER NOT NULL,
                    `downloadedLength` INTEGER NOT NULL, `isDownloading` INTEGER NOT NULL,
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)"""
                )
                db.execSQL(
                    """INSERT INTO `HanimeDownloadEntity`(
                        `coverUrl`, `title`, `addDate`,
                        `videoCode`, `videoUri`, `quality`,
                        `videoUrl`, `length`, `downloadedLength`, `isDownloading`, `id`)
                     SELECT `coverUrl`, `title`, `addDate`, `videoCode`, `videoUri`, `quality`,
                        '' AS `videoUrl`, 1 AS `length`, 1 AS `downloadedLength`, 0 AS `isDownloading`,
                        `id`
                     FROM `HanimeDownloadedEntity`"""
                )
                db.execSQL("""DROP TABLE IF EXISTS HanimeDownloadedEntity""")
            }
        }
    }
}