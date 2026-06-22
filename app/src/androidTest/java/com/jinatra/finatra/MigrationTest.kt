package com.jinatra.finatra

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jinatra.finatra.data.local.FinatraDatabase
import com.jinatra.finatra.data.local.MIGRATION_1_2
import com.jinatra.finatra.data.local.MIGRATION_2_3
import com.jinatra.finatra.data.local.MIGRATION_3_4
import com.jinatra.finatra.data.local.MIGRATION_4_5
import com.jinatra.finatra.data.local.MIGRATION_5_6
import com.jinatra.finatra.data.local.MIGRATION_6_7
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Room schema migrations (v1 through v5).
 *
 * Each test creates the database at an old version with [MigrationTestHelper], inserts data using
 * that version's schema, runs the real migration, validates the schema against the current
 * entities, and asserts that pre-existing rows survived and new columns/tables behave correctly.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FinatraDatabase::class.java,
    )

    /** v1 -> v2: an existing account row survives and the new `lowBalanceThreshold` column defaults to 0. */
    @Test
    fun migrate1To2_preservesAccountsAndAddsThreshold() {
        // 1) Create the v1 database and insert an account using the OLD schema (no lowBalanceThreshold).
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO accounts " +
                    "(name, type, currency, openingBalance, colorHex, iconKey, archived, createdAt) " +
                    "VALUES ('Cash', 'CASH', 'USD', 1000.0, 4283212100, 'wallet', 0, 1700000000000)",
            )
            close()
        }

        // 2) Run the real migration to v2 and validate the resulting schema matches the entities.
        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        // 3) The pre-existing row survived, and the new column defaulted to 0.
        db.query("SELECT name, openingBalance, lowBalanceThreshold FROM accounts").use { c ->
            assertTrue("expected the migrated account row", c.moveToFirst())
            assertEquals("Cash", c.getString(0))
            assertEquals(1000.0, c.getDouble(1), 0.001)
            assertEquals(0.0, c.getDouble(2), 0.001)
        }
    }

    /** v2 -> v3 adds the goals + chat_messages tables and preserves existing data. */
    @Test
    fun migrate2To3_preservesDataAndAddsTables() {
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                "INSERT INTO accounts " +
                    "(name, type, currency, openingBalance, colorHex, iconKey, archived, lowBalanceThreshold, createdAt) " +
                    "VALUES ('Cash', 'CASH', 'USD', 1000.0, 4283212100, 'wallet', 0, 0.0, 1700000000000)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 3, true, MIGRATION_2_3)

        // Pre-existing account survived.
        db.query("SELECT name FROM accounts").use { c ->
            assertTrue(c.moveToFirst()); assertEquals("Cash", c.getString(0))
        }
        // New tables are usable.
        db.execSQL(
            "INSERT INTO goals (name, type, targetAmount, savedAmount, currency, deadline, colorHex, iconKey, createdAt) " +
                "VALUES ('Emergency', 'SAVINGS', 5000.0, 100.0, 'USD', NULL, 175362, 'goal', 1700000000000)",
        )
        db.query("SELECT name, targetAmount FROM goals").use { c ->
            assertTrue(c.moveToFirst()); assertEquals("Emergency", c.getString(0)); assertEquals(5000.0, c.getDouble(1), 0.001)
        }
        db.execSQL("INSERT INTO chat_messages (role, content, timestamp) VALUES ('user', 'hi', 1700000000000)")
        db.query("SELECT COUNT(*) FROM chat_messages").use { c ->
            assertTrue(c.moveToFirst()); assertEquals(1, c.getInt(0))
        }
    }

    /** v3 -> v4 adds transactions.splitGroupId (nullable) and preserves existing rows. */
    @Test
    fun migrate3To4_addsSplitGroupId() {
        helper.createDatabase(dbName, 3).apply {
            execSQL(
                "INSERT INTO accounts " +
                    "(name, type, currency, openingBalance, colorHex, iconKey, archived, lowBalanceThreshold, createdAt) " +
                    "VALUES ('Cash', 'CASH', 'USD', 0.0, 4283212100, 'wallet', 0, 0.0, 1700000000000)",
            )
            execSQL(
                "INSERT INTO transactions " +
                    "(type, amount, currency, dateTime, accountId, note, tags, createdAt, updatedAt) " +
                    "VALUES ('EXPENSE', 50.0, 'USD', 1700000000000, 1, 'lunch', '', 1700000000000, 1700000000000)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 4, true, MIGRATION_3_4)

        db.query("SELECT note, splitGroupId FROM transactions").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("lunch", c.getString(0))
            assertTrue("splitGroupId defaults to null", c.isNull(1))
        }
    }

    /** v4 -> v5 adds the tx_templates table and is usable. */
    @Test
    fun migrate4To5_addsTemplates() {
        helper.createDatabase(dbName, 4).apply { close() }
        val db = helper.runMigrationsAndValidate(dbName, 5, true, MIGRATION_4_5)
        db.execSQL(
            "INSERT INTO tx_templates (name, type, amount, categoryId, accountId, note, tags, createdAt) " +
                "VALUES ('Coffee', 'EXPENSE', 150.0, NULL, NULL, 'Latte', '', 1700000000000)",
        )
        db.query("SELECT name, amount FROM tx_templates").use { c ->
            assertTrue(c.moveToFirst()); assertEquals("Coffee", c.getString(0)); assertEquals(150.0, c.getDouble(1), 0.001)
        }
    }

    /** v5 -> v6 adds chat_sessions and rebuilds chat_messages with a sessionId FK; existing AI Coach
     *  messages are folded into a single "Previous chat" coach session so no history is lost. */
    @Test
    fun migrate5To6_foldsLegacyChatIntoSession() {
        helper.createDatabase(dbName, 5).apply {
            execSQL("INSERT INTO chat_messages (role, content, timestamp) VALUES ('user', 'old message', 1700000000000)")
            execSQL("INSERT INTO chat_messages (role, content, timestamp) VALUES ('ai', 'old reply', 1700000000001)")
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 6, true, MIGRATION_5_6)

        // Both legacy messages survived and were assigned to one created session.
        db.query("SELECT DISTINCT sessionId FROM chat_messages").use { c ->
            assertTrue(c.moveToFirst()); assertTrue("messages got a session", c.getLong(0) > 0)
            assertEquals("all under one session", 1, c.count)
        }
        db.query("SELECT COUNT(*) FROM chat_messages").use { c ->
            assertTrue(c.moveToFirst()); assertEquals(2, c.getInt(0))
        }
        db.query("SELECT kind, title FROM chat_sessions").use { c ->
            assertTrue(c.moveToFirst()); assertEquals("coach", c.getString(0)); assertEquals("Previous chat", c.getString(1))
        }
    }

    /** v5 -> v6 with no prior history: no legacy session is created, and the new schema is usable. */
    @Test
    fun migrate5To6_emptyChat_createsNoSessionAndIsUsable() {
        helper.createDatabase(dbName, 5).apply { close() }
        val db = helper.runMigrationsAndValidate(dbName, 6, true, MIGRATION_5_6)

        db.query("SELECT COUNT(*) FROM chat_sessions").use { c ->
            assertTrue(c.moveToFirst()); assertEquals(0, c.getInt(0))
        }
        // New session + message round-trips through the rebuilt schema.
        db.execSQL("INSERT INTO chat_sessions (kind, title, createdAt, updatedAt) VALUES ('budget', 'Budget plan', 0, 0)")
        db.execSQL("INSERT INTO chat_messages (sessionId, role, content, timestamp) VALUES (1, 'ai', 'hi', 0)")
        db.query("SELECT content FROM chat_messages WHERE sessionId = 1").use { c ->
            assertTrue(c.moveToFirst()); assertEquals("hi", c.getString(0))
        }
    }

    /** v6 -> v7 adds goal plan columns (default 0 / null) and the loans table, preserving goals. */
    @Test
    fun migrate6To7_addsGoalPlanAndLoans() {
        helper.createDatabase(dbName, 6).apply {
            execSQL(
                "INSERT INTO goals (name, type, targetAmount, savedAmount, currency, deadline, colorHex, iconKey, createdAt) " +
                    "VALUES ('Car', 'SAVINGS', 10000.0, 2000.0, 'USD', NULL, 175362, 'goal', 1700000000000)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 7, true, MIGRATION_6_7)

        // Existing goal survived; new plan columns defaulted.
        db.query("SELECT name, monthlyTarget, planStartedAt FROM goals").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Car", c.getString(0))
            assertEquals(0.0, c.getDouble(1), 0.001)
            assertTrue("planStartedAt defaults to null", c.isNull(2))
        }
        // Loans table is usable.
        db.execSQL(
            "INSERT INTO loans (name, principal, annualRatePct, tenureMonths, startDate, currency, createdAt) " +
                "VALUES ('Auto loan', 12000.0, 9.5, 36, 1700000000000, 'USD', 1700000000000)",
        )
        db.query("SELECT name, tenureMonths FROM loans").use { c ->
            assertTrue(c.moveToFirst()); assertEquals("Auto loan", c.getString(0)); assertEquals(36, c.getInt(1))
        }
    }
}
