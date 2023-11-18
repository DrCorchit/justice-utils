package com.drcorchit.justice.utils.logging

import com.drcorchit.justice.utils.Uri

class UriLoggerImpl(val clazz: Class<*>, override val uri: Uri) : UriLogger, Logger by Logger.getLogger(clazz) {
    override fun child(name: String): UriLogger {
        return UriLoggerImpl(clazz, uri.extend(name))
    }
}