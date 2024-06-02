package com.drcorchit.justice.utils.logging

import com.drcorchit.justice.utils.Utils.createCache

abstract class UriLogger(val uri: Uri) : Logger() {

    abstract fun child(name: String): UriLogger

    override fun getCallsite(): String {
        return "$uri.${super.getCallsite()}"
    }

    private class UriLoggerImpl(private val clazz: Class<*>, uri: Uri) : UriLogger(uri) {
        val logger = Logger.getLogger(clazz)

        override fun log(info: LogInfo) {
            return logger.log(info)
        }

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