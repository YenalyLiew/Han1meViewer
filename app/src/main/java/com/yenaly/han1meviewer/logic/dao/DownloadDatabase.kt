package com.yenaly.han1meviewer.logic.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yenaly.han1meviewer.logic.dao.download.DownloadCategoryDao
import com.yenaly.han1meviewer.logic.dao.download.HanimeDownloadDao
import com.yenaly.han1meviewer.logic.entity.download.DownloadCategoryEntity
import com.yenaly.han1meviewer.logic.entity.download.HanimeCategoryCrossRef
import com.yenaly.han1meviewer.logic.entity.download.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.state.DownloadState
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/07 007 18:26
 */
@Database(
    entities = [HanimeDownloadEntity::class, DownloadCategoryEntity::class, HanimeCategoryCrossRef::class],
    version = 3, exportSchema = false
)
abstract class DownloadDatabase : RoomDatabase() {

    abstract val hanimeDownloadDao: HanimeDownloadDao
    abstract val downloadCategoryDao: DownloadCategoryDao

    companion object {
        val instance by lazy {
            Room.databaseBuilder(
                applicationContext,
                DownloadDatabase::class.java,
                "download.db"
            ).addMigrations(Migration1To2, Migration2To3).build()
        }
    }

    object Migration1To2 : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `HanimeDownloadEntity`(
                    `coverUrl` TEXT NOT NULL, `title` TEXT NOT NULL,
                    `addDate` INTEGER NOT NULL, `videoCode` TEXT NOT NULL,
                    `videoUri` TEXT NOT NULL, `quality` TEXT NOT NULL,
                    `videoUrl` TEXT NOT NULL, `length` INTEGER NOT NULL,
                    `downloadedLength` INTEGER NOT NULL, `isDownloading` INTEGER NOT NULL,
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)""".trimIndent()
            )
            db.execSQL(
                """INSERT INTO `HanimeDownloadEntity`(
                        `coverUrl`, `title`, `addDate`,
                        `videoCode`, `videoUri`, `quality`,
                        `videoUrl`, `length`, `downloadedLength`, `isDownloading`, `id`)
                     SELECT `coverUrl`, `title`, `addDate`, `videoCode`, `videoUri`, `quality`,
                        '' AS `videoUrl`, 1 AS `length`, 1 AS `downloadedLength`, 0 AS `isDownloading`,
                        `id`
                     FROM `HanimeDownloadedEntity`""".trimIndent()
            )
            db.execSQL("""DROP TABLE IF EXISTS HanimeDownloadedEntity""")
        }
    }

    object Migration2To3 : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `DownloadCategoryEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)"""
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `HanimeCategoryCrossRef` (`videoId` INTEGER NOT NULL, `categoryId` INTEGER NOT NULL, PRIMARY KEY(`videoId`, `categoryId`))"""
            )
            // Add coverUri column
            db.execSQL("""ALTER TABLE `HanimeDownloadEntity` ADD COLUMN `coverUri` TEXT NULL""")

            // Add state column with default value (convert from isDownloading)
            db.execSQL("""ALTER TABLE `HanimeDownloadEntity` ADD COLUMN `state` INTEGER NOT NULL DEFAULT ${DownloadState.Mask.UNKNOWN}""")

            // Update state values based on isDownloading
            // If isDownloading=1, set state to DOWNLOADING (2)
            // If isDownloading=0,
            //                     if downloadedLength=length, set state to FINISHED (4)
            //                     else set state to PAUSED (3)
            db.execSQL(
                """UPDATE `HanimeDownloadEntity` SET `state` = 
                    |CASE WHEN `isDownloading` = 1 THEN ${DownloadState.Mask.DOWNLOADING} ELSE 
                    |CASE WHEN `downloadedLength` = `length` THEN ${DownloadState.Mask.FINISHED} 
                    |ELSE ${DownloadState.Mask.PAUSED} END END""".trimMargin()
            )
        }
    }
}