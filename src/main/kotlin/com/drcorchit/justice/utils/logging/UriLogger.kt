package com.drcorchit.justice.utils.logging

interface UriLogger: Logger {

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
}