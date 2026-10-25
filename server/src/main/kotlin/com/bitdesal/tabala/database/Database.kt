package com.bitdesal.tabala.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.flywaydb.core.Flyway
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.File

private const val BUSY_TIMEOUT_MS = 5_000

/**
 * Opens the SQLite database at [path], applies pending Flyway migrations and returns a driver
 * whose connections all enforce foreign keys and use WAL (required by Litestream).
 */
fun openDatabase(path: String): SqlDriver {
    File(path).absoluteFile.parentFile?.mkdirs()
    val url = "jdbc:sqlite:$path"
    val config = SQLiteConfig().apply {
        enforceForeignKeys(true)
        setJournalMode(SQLiteConfig.JournalMode.WAL)
        setBusyTimeout(BUSY_TIMEOUT_MS)
    }

    Flyway.configure()
        .dataSource(SQLiteDataSource(config).apply { setUrl(url) })
        .load()
        .migrate()

    return JdbcSqliteDriver(url, config.toProperties())
}
