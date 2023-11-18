package com.drcorchit.justice.utils.logging

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

class Log4jLogger(clazz: Class<*>): Logger {
    private val logger = LogManager.getLogger(clazz)

    override fun debug(method: String, message: String) {
        log(LogInfo(Level.DEBUG, method, message, null))
    }

    override fun info(method: String, message: String) {
        log(LogInfo(Level.INFO, method, message, null))
    }

    override fun warn(method: String, message: String) {
        log(LogInfo(Level.WARN, method, message, null))
    }

    override fun error(method: String, message: String, error: Throwable?) {
        log(LogInfo(Level.ERROR, method, message, error))
    }

    override fun fatal(method: String, message: String, error: Throwable?) {
        log(LogInfo(Level.FATAL, method, message, error))
    }

    private fun log(info: LogInfo) {
        if (info.error == null) {
            logger.log(info.level, info.logString)
        } else {
            logger.log(info.level, info.logString, info.error)
        }
    }
}