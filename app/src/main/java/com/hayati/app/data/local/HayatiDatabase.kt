package com.hayati.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hayati.app.data.adhkar.local.AdhkarProgressDao
import com.hayati.app.data.adhkar.local.AdhkarProgressEntity
import com.hayati.app.data.prayer.local.PrayerLogDao
import com.hayati.app.data.prayer.local.PrayerLogEntity

/**
 * Local database. Schema export is off for version 1; turn it on (and commit the schema JSON)
 * before the first schema change so migrations can be tested.
 */
@Database(entities = [TaskEntity::class, PrayerLogEntity::class, AdhkarProgressEntity::class], version = 3, exportSchema = false)
abstract class HayatiDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun prayerLogDao(): PrayerLogDao

    abstract fun adhkarProgressDao(): AdhkarProgressDao

    companion object {
        const val NAME = "hayati.db"

        /** v2 -> v3: adds the adhkar counter table. Existing tables are not touched. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `adhkar_progress` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`progressDate` INTEGER NOT NULL, " +
                        "`category` TEXT NOT NULL, " +
                        "`dhikrId` TEXT NOT NULL, " +
                        "`count` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_adhkar_progress_progressDate_category_dhikrId` " +
                        "ON `adhkar_progress` (`progressDate`, `category`, `dhikrId`)",
                )
            }
        }

        /** v1 -> v2: adds the prayer tracking table. The tasks table is not touched. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `prayer_logs` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`prayerDate` INTEGER NOT NULL, " +
                        "`prayerType` TEXT NOT NULL, " +
                        "`status` TEXT NOT NULL, " +
                        "`completedAt` INTEGER, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_prayer_logs_prayerDate_prayerType` " +
                        "ON `prayer_logs` (`prayerDate`, `prayerType`)",
                )
            }
        }
    }
}
