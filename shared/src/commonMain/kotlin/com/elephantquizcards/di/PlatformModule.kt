package com.elephantquizcards.di

import org.koin.core.module.Module

/**
 * Платформенные привязки графа: драйвер БД и всё, что отличается по платформам.
 *
 * Реализации (`actual`) находятся в `androidMain` / `jvmMain` / `iosMain`.
 */
expect val platformModule: Module
