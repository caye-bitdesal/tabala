package com.bitdesal.tabala.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DatabaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun databasePath() = tempFolder.root.resolve("nested/test.db").path

    @Test
    fun `should apply migrations and seed position types when database is new`() {
        openDatabase(databasePath()).use { driver ->
            assertEquals(1L, driver.queryLong("SELECT count(*) FROM flyway_schema_history WHERE success = 1"))
            assertEquals(10L, driver.queryLong("SELECT count(*) FROM position_types"))
        }
    }

    @Test
    fun `should not reapply migrations when database is reopened`() {
        val path = databasePath()
        openDatabase(path).close()

        openDatabase(path).use { driver ->
            assertEquals(1L, driver.queryLong("SELECT count(*) FROM flyway_schema_history WHERE success = 1"))
            assertEquals(10L, driver.queryLong("SELECT count(*) FROM position_types"))
        }
    }

    @Test
    fun `should enable WAL and foreign keys on driver connections`() {
        openDatabase(databasePath()).use { driver ->
            assertEquals("wal", driver.queryString("PRAGMA journal_mode"))
            assertEquals(1L, driver.queryLong("PRAGMA foreign_keys"))
        }
    }

    @Test
    fun `should reject position referencing unknown figure`() {
        openDatabase(databasePath()).use { driver ->
            assertFailsWith<Exception> {
                driver.execute(
                    null,
                    """
                    INSERT INTO positions (id, figure_id, position_type_id, x, y, z, created_at, updated_at)
                    VALUES ('p1', 'missing', 'segon', 0, 0, 0, 't', 't')
                    """.trimIndent(),
                    0
                )
            }
        }
    }

    private fun SqlDriver.queryLong(sql: String): Long? =
        executeQuery(null, sql, { cursor -> QueryResult.Value(cursor.takeIf { it.next().value }?.getLong(0)) }, 0).value

    private fun SqlDriver.queryString(sql: String): String? =
        executeQuery(null, sql, { cursor -> QueryResult.Value(cursor.takeIf { it.next().value }?.getString(0)) }, 0).value
}
