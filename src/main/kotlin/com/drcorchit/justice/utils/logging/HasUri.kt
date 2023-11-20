package com.drcorchit.justice.utils.logging

interface HasUri {
    val parent: HasUri?
    val uri: Uri

    val logger: UriLogger get() = UriLogger.getLogger(this)
}