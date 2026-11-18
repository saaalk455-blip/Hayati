package com.hayati.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hayati.app.data.prayer.local.PrayerLogDao
import com.hayati.app.data.prayer.local.PrayerLogEntity

/**
 * Local database. Schema export is off for version 1; turn it on (and commit the schema JSON)
 * before the first schema change so migrations can be tested.
 */
@Database(entities = [TaskEntity::class, PrayerLogEntity::class], version = 2, exportSchema = false)
abstract class HayatiDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun prayerLogDao(): PrayerLogDao

    companion object {
        const val NAME = "hayati.db"

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
