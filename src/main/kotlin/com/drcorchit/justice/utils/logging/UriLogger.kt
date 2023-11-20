package com.drcorchit.justice.utils.logging

import com.drcorchit.justice.utils.Utils.createCache

interface UriLogger : Logger {

    val uri: Uri

    fun child(name: String): UriLogger

    fun debug(message: String) {
        debug(uri.value, message)
    }

    fun info(message: String) {
        info(uri.value, message)
    }

    fun warn(message: String) {
        warn(uri.value, message)
    }

    fun error(message: String, error: Throwable? = null) {
        error(uri.value, message, error)
    }

    fun fatal(message: String, error: Throwable? = null) {
        fatal(uri.value, message, error)
    }

    private class UriLoggerImpl(private val clazz: Class<*>, override val uri: Uri) : UriLogger,
        Logger by Logger.getLogger(clazz) {
        override fun child(name: String): UriLogger {
            return UriLoggerImpl(clazz, uri.extend(name))
        }
    }

    companion object {
        private val cache = createCache<HasUri, UriLogger>(10000) {
            UriLoggerImpl(it::class.java, it.uri)
        }

        fun getLogger(any: HasUri): UriLogger {
            return cache.get(any)
        }
    }
}