package com.drcorchit.justice.utils.logging

import com.drcorchit.justice.utils.data.Trie


interface HasUri {
    val parent: HasUri?
    val uri: Uri

    fun getLogger(): UriLogger {
        return cache.computeIfAbsent(uri.getParts()) {
            parent?.getLogger()?.child(uri.value) ?: UriLoggerImpl(this::class.java, uri)
        }
    }

    companion object {
        private val cache = Trie<String, UriLogger>()
    }
}