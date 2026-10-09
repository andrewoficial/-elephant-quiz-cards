package com.elephantquizcards.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory(private val databasePath: String = "quizcards.db") {
    actual fun createDriver(): SqlDriver {
        val isNew = !File(databasePath).exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")
        if (isNew) {
            Database.Schema.create(driver)
        }
        return driver
    }
}
