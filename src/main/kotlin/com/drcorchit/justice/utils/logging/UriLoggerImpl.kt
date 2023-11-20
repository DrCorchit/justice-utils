package com.drcorchit.justice.utils.logging

class UriLoggerImpl(val clazz: Class<*>, override val uri: Uri) : UriLogger, Logger by Logger.getLogger(clazz) {
    override fun child(name: String): UriLogger {
        return UriLoggerImpl(clazz, uri.extend(name))
    }
}