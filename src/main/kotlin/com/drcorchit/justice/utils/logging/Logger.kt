package com.drcorchit.justice.utils.logging

import java.util.concurrent.ConcurrentHashMap

interface Logger {
    fun debug(method: String, message: String)

    fun info(method: String, message: String)

    fun warn(method: String, message: String)

    fun error(method: String, message: String, error: Throwable? = null)

    fun fatal(method: String, message: String, error: Throwable? = null)

    companion object {
        private val LOGGERS = ConcurrentHashMap<Class<*>, Logger>()

        @JvmStatic
        fun getLogger(clazz: Class<*>): Logger {
            return LOGGERS.computeIfAbsent(clazz) { Log4jLogger(it) }
        }
    }
}