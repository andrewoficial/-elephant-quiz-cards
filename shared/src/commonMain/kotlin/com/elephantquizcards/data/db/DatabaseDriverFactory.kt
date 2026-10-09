package com.elephantquizcards.data.db

import app.cash.sqldelight.db.SqlDriver

/** Создаёт платформенный драйвер SQLDelight для БД `Database`. */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
