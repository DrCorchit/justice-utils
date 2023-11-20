package com.drcorchit.justice.utils.data

import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class TrieTest {

    @Test
    fun basicsTest() {
        val trie = Trie<String, String>()
        val key1 = listOf("a", "b")
        val key2 = listOf("a", "b", "c")
        trie[key1] = "Hello"
        trie[key2] = "World"
        Assertions.assertEquals("Hello", trie[key1])
        Assertions.assertEquals("World", trie[key2])
    }

    @Test
    fun entriesTest() {
        val trie = Trie<String, String>()
        val keys = listOf(
            listOf(),
            listOf("a"),
            listOf("a", "b"),
            listOf("a", "b", "c"),
            listOf("b", "b", "a"),
            listOf("b", "c", "c", "d"),
            listOf("b", "c", "c", "e"),
            listOf("b", "d", "d", "f", "g"),
            listOf("c", "d", "c", "d", "e")
        )
        val values = listOf(
            "The", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog"
        )

        //Set up the trie
        keys.zip(values).forEach { trie[it.first] = it.second }
        //Test that the returned entries reflect the underlying structure
        trie.entries.forEachIndexed { index, entry ->
            Assertions.assertEquals(keys[index], entry.key)
            Assertions.assertEquals(values[index], entry.value)
        }
    }

    @Test
    fun prefixTest() {
        val trie = Trie<String, String>()
        val keys = listOf(
            listOf(),
            listOf("a"),
            listOf("a", "b"),
            listOf("a", "b", "c"),
            listOf("a", "b", "a"),
            listOf("b", "c", "c", "d"),
            listOf("b", "c", "c", "e"),
            listOf("b", "d", "d", "f", "g"),
            listOf("c", "d", "c", "d", "e")
        )
        val values = listOf(
            "The", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog"
        )

        //Set up the trie
        keys.zip(values).forEach { trie[it.first] = it.second }

        Assertions.assertEquals(listOf("brown", "fox", "jumped"), trie.getPrefix(listOf("a", "b")))
        Assertions.assertEquals(listOf("over", "the"), trie.getPrefix(listOf("b", "c")))
    }
}