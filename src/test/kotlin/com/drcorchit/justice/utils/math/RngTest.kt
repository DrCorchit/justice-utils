package com.drcorchit.justice.utils.math

import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class RngTest {
    @Test
    fun seedTest() {
        val rng = Rng(0)
        Assertions.assertEquals(0, rng.getSeed())
        val real1 = rng.nextReal()
        val seed = rng.getSeed()
        rng.setSeed(0)
        val real2 = rng.nextReal()
        Assertions.assertEquals(real1, real2)
        Assertions.assertEquals(seed, rng.getSeed())
    }
}