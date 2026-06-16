package com.jinatra.finatra

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jinatra.finatra.data.local.FinatraDatabase
import com.jinatra.finatra.data.local.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Proves the v1 -> v2 Room migration keeps existing data and adds the new column with its default. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FinatraDatabase::class.java,
    )

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
}
