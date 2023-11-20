package com.drcorchit.justice.utils.logging

import com.drcorchit.justice.utils.data.Trie


interface HasUri {
    val name: String
    val parent: HasUri?
    val uri: Uri get() = Uri(parent?.uri, name)

    fun getLogger(): UriLogger {
        return cache.computeIfAbsent(uri.getParts()) { UriLoggerImpl(this::class.java, uri) }
    }

    companion object {
        private val cache = Trie<String, UriLogger>()
    }
}