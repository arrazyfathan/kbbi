package com.arrazyfathan.kbbi.core.logging

import timber.log.Timber

private const val ANDROID_LOG_CHUNK_SIZE = 3_500

object AppLogger {
    private var debugTreePlanted = false

    private enum class LogLevel {
        Verbose,
        Debug,
        Info,
        Warning,
        Error,
        Critical,
    }

    fun plantDebugTree() {
        if (debugTreePlanted) return

        Timber.plant(Timber.DebugTree())
        debugTreePlanted = true
    }

    fun verbose(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Verbose, tag, message, throwable)
    }

    fun debug(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Debug, tag, message, throwable)
    }

    fun info(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Info, tag, message, throwable)
    }

    fun warning(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Warning, tag, message, throwable)
    }

    fun warn(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        warning(tag, message, throwable)
    }

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Error, tag, message, throwable)
    }

    fun error(
        tag: String,
        throwable: Throwable,
        message: String,
    ) {
        error(tag = tag, message = message, throwable = throwable)
    }

    fun critical(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Critical, tag, message, throwable)
    }

    fun throwable(
        tag: String,
        throwable: Throwable,
        message: String = throwable.message ?: throwable::class.java.simpleName,
    ) {
        error(tag = tag, message = message, throwable = throwable)
    }

    private fun write(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        message.toLogChunks().forEachIndexed { index, chunk ->
            val throwableForChunk = throwable.takeIf { index == 0 }
            Timber.tag(tag).writeChunk(level, chunk, throwableForChunk)
        }
    }

    private fun String.toLogChunks(): List<String> =
        chunked(ANDROID_LOG_CHUNK_SIZE).ifEmpty {
            listOf("")
        }

    private fun Timber.Tree.writeChunk(
        level: LogLevel,
        message: String,
        throwable: Throwable?,
    ) {
        when (level) {
            LogLevel.Verbose -> v(throwable, message)
            LogLevel.Debug -> d(throwable, message)
            LogLevel.Info -> i(throwable, message)
            LogLevel.Warning -> w(throwable, message)
            LogLevel.Error -> e(throwable, message)
            LogLevel.Critical -> wtf(throwable, message)
        }
    }
}
