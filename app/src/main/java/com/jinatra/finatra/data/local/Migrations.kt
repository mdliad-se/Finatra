package com.jinatra.finatra.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v1 -> v2: per-account low-balance alert threshold (PRD 6.10). */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE accounts ADD COLUMN lowBalanceThreshold REAL NOT NULL DEFAULT 0.0")
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
