package com.drcorchit.justice.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.*
import org.apache.logging.log4j.LogManager

class Logger private constructor(clazz: Class<*>) {
    //Civplanet logger is currently a facade for this class
    private val logger: org.apache.logging.log4j.Logger

    init {
        logger = LogManager.getLogger(clazz)
        //info("init", "Created logger.");
    }

    fun debug(method: String, message: String) {
        log(LogInfo(DEBUG, method, message, null))
    }

    fun info(method: String, message: String) {
        log(LogInfo(INFO, method, message, null))
    }

    fun warn(method: String, message: String) {
        log(LogInfo(WARN, method, message, null))
    }

    fun error(method: String, message: String) {
        log(LogInfo(ERROR, method, message, null))
    }

    fun error(method: String, message: String, error: Throwable?) {
        log(LogInfo(ERROR, method, message, error))
    }

    fun fatal(method: String, message: String) {
        log(LogInfo(FATAL, method, message, null))
    }

    fun fatal(method: String, message: String, error: Throwable?) {
        log(LogInfo(FATAL, method, message, error))
    }

    private fun log(info: LogInfo) {
        if (info.error == null) {
            logger.log(info.level, info.logString)
        } else {
            logger.log(info.level, info.logString, info.error)
        }
    }

    inner class LogInfo constructor(level: Level, method: String, message: String, error: Throwable?) {
        val level: Level
        val method: String
        val message: String
        val error: Throwable?

        init {
            this.level = level
            this.method = method
            this.message = message
            this.error = error
        }

        val logString: String
            get() = String.format("[%s] %s", method, message)

        override fun toString(): String {
            val builder = StringBuilder()
            builder.append(level.name()).append(" [").append(method).append("] ").append(message)
            if (error != null) {
                builder.append(System.lineSeparator()).append(error.stackTraceToString())
            }
            return builder.toString()
        }
    }

    companion object {
        private val LOGGERS = HashMap<Class<*>, Logger>()
        @Synchronized
        fun getLogger(clazz: Class<*>): Logger {
            return LOGGERS.computeIfAbsent(clazz) { Logger(it) }
        }
    }
}