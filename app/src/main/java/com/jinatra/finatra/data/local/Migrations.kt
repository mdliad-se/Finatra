package com.jinatra.finatra.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v1 -> v2: per-account low-balance alert threshold (PRD 6.10). */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE accounts ADD COLUMN lowBalanceThreshold REAL NOT NULL DEFAULT 0.0")
    }
}

/** v2 -> v3: savings goals / debt tracker + AI Coach chat history (PRD 6.9, 6.11). */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                targetAmount REAL NOT NULL,
                savedAmount REAL NOT NULL,
                currency TEXT NOT NULL,
                deadline INTEGER,
                colorHex INTEGER NOT NULL,
                iconKey TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS chat_messages (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

/** v3 -> v4: link split-transaction parts via a shared group id (PRD 6.4). */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN splitGroupId INTEGER")
    }
}

/** v4 -> v5: saved transaction templates (PRD 6.4). */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tx_templates (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                amount REAL NOT NULL,
                categoryId INTEGER,
                accountId INTEGER,
                note TEXT NOT NULL,
                tags TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

/** All schema migrations in order, handed to Room's builder so existing installs upgrade
 *  step-by-step from v1 through the current version without data loss. */
val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
