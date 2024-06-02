package com.drcorchit.justice.utils.logging

import org.apache.logging.log4j.Level
import java.util.concurrent.ConcurrentHashMap

abstract class Logger {
    abstract fun log(info: LogInfo)

    open fun getCallsite(): String {
        val stack = Thread.currentThread().stackTrace
        return stack.firstOrNull {
            !logMethodNames.contains(it.methodName)
        }?.let { "${it.methodName}:${it.lineNumber}" } ?: "unknown"
    }

    fun debug(message: String) {
        log(LogInfo(Level.DEBUG, getCallsite(), message, null))
    }

    fun info(message: String) {
        log(LogInfo(Level.INFO, getCallsite(), message, null))
    }

    fun warn(message: String) {
        log(LogInfo(Level.WARN, getCallsite(), message, null))
    }

    fun error(message: String, error: Throwable?) {
        log(LogInfo(Level.ERROR, getCallsite(), message, error))
    }

    fun fatal(message: String, error: Throwable?) {
        log(LogInfo(Level.FATAL, getCallsite(), message, error))
    }

    companion object {

        val logMethodNames = setOf("getStackTrace", "getCallsite", "log", "debug", "info", "warn", "error", "fatal")

        private var logProvider: (Class<*>) -> Logger = { Log4jLogger(it) }

        @JvmStatic
        fun setLogProvider(provider: (Class<*>) -> Logger) {
            logProvider = provider
        }

        private val LOGGERS = ConcurrentHashMap<Class<*>, Logger>()

        @JvmStatic
        fun getLogger(clazz: Class<*>): Logger {
            return LOGGERS.computeIfAbsent(clazz) { logProvider.invoke(clazz) }
        }
    }
}