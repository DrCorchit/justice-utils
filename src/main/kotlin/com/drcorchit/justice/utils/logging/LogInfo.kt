package com.drcorchit.justice.utils.logging

import org.apache.logging.log4j.Level

data class LogInfo(val level: Level, val method: String, val message: String, val error: Throwable?) {

    val logString = "[$method] $message"

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(level.name()).append(" [").append(method).append("] ").append(message)
        if (error != null) {
            builder.append(System.lineSeparator()).append(error.stackTraceToString())
        }
        return builder.toString()
    }
}