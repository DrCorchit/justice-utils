package com.drcorchit.justice.utils

import com.drcorchit.justice.utils.Utils.binarySearch
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class UtilsTest {

    @Test
    fun testBinarySearch() {
        val input1 = listOf(1, 2, 3, 4, 5, 6)
        Assertions.assertEquals(2, input1.binarySearch(2.7) { it })

        val input2 = listOf(1.618, 2.718, 3.14159, 4.141492614, 5)
        Assertions.assertEquals(0, input2.binarySearch(1) { it })
        Assertions.assertEquals(0, input2.binarySearch(2) { it })
        Assertions.assertEquals(1, input2.binarySearch(2.5) { it })
        Assertions.assertEquals(2, input2.binarySearch(3) { it })
        Assertions.assertEquals(3, input2.binarySearch(4) { it })
        Assertions.assertEquals(4, input2.binarySearch(5) { it })
        Assertions.assertEquals(4, input2.binarySearch(100) { it })
        Assertions.assertEquals(0, input2.binarySearch(-100) { it })
    }

}