package com.drcorchit.utils.math

import com.drcorchit.utils.clamp
import java.util.concurrent.atomic.AtomicLong

/**
 * The code in this file is mostly copies from java.util.Random
 * The reason Random itself cannot be used directly is twofold:
 * First, Random does not allow us to get the seed, which is important
 * when loading gamestate.
 * Second, this class is enhanced with several features such as random variables.
 */
class Rng(seed: Long = seedUniquifier() xor System.nanoTime()) {
    private val seed: AtomicLong
    private var nextGaussian = 0.0
    private var haveNextGaussian = false

    init {
        this.seed = AtomicLong(initialScramble(seed))
    }

    fun copy(): Rng {
        return Rng(getSeed())
    }

    fun getSeed(): Long {
        return initialScramble(seed.get())
    }

    fun setSeed(seed: Long) {
        this.seed.set(initialScramble(seed))
        haveNextGaussian = false
    }

    private fun next(bits: Int): Int {
        var oldseed: Long
        var nextseed: Long
        val seed = seed
        do {
            oldseed = seed.get()
            nextseed = oldseed * multiplier + addend and mask
        } while (!seed.compareAndSet(oldseed, nextseed))
        return (nextseed ushr 48 - bits).toInt()
    }

    fun nextBoolean(): Boolean {
        return next(1) != 0
    }

    fun nextInteger(): Long {
        return (next(32).toLong() shl 32) + next(32)
    }

    fun nextInteger(bound: Long): Long {
        require(bound > 0) { "Bound cannot be negative!" }
        val integer = Math.abs(nextInteger())
        val max = Long.MAX_VALUE / bound * bound
        return if (integer >= max) {
            //the result cannot be used, since it is skewed towards the lower values of bound. Try again.
            nextInteger(bound)
        } else {
            //keep the result within the bound
            integer % bound
        }
    }

    //Returns a real number (double) from 0-1
    fun nextReal(): Double {
        return ((next(26).toLong() shl 27) + next(27)) * DOUBLE_UNIT
    }

    @Synchronized
    fun nextGaussian(): Double {
        // See Knuth, ACP, Section 3.4.1 Algorithm C.
        return if (haveNextGaussian) {
            haveNextGaussian = false
            nextGaussian
        } else {
            var v1: Double
            var v2: Double
            var s: Double
            do {
                v1 = 2 * nextReal() - 1 // between -1 and 1
                v2 = 2 * nextReal() - 1 // between -1 and 1
                s = v1 * v1 + v2 * v2
            } while (s >= 1 || s == 0.0)
            val multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s)
            nextGaussian = v2 * multiplier
            haveNextGaussian = true
            v1 * multiplier
        }
    }

    fun normal(mean: Double, std: Double): NormalRandomVariable {
        return NormalRandomVariable(mean, std)
    }

    fun clampedNormal(mean: Double, std: Double, range: Double): ClampedNormalRandomVariable {
        return ClampedNormalRandomVariable(mean, std, range)
    }

    fun dice(numDice: Int, sidesPerDie: Int): DiscreetRandomVariable {
        return DiscreetRandomVariable(numDice, sidesPerDie)
    }

    fun uniform(min: Double, max: Double): UniformRandomVariable {
        return UniformRandomVariable(min, max)
    }

    fun constant(value: Double): UniformRandomVariable {
        return UniformRandomVariable(value, value)
    }

    abstract class RandomVariable {
        abstract operator fun next(): Double
        abstract fun average(): Double
    }

    open inner class NormalRandomVariable(private val mean: Double, private val std: Double) : RandomVariable() {
        override fun next(): Double {
            return mean + std * nextGaussian()
        }

        override fun average(): Double {
            return mean
        }
    }

    inner class ClampedNormalRandomVariable(mean: Double, std: Double, private val range: Double) : NormalRandomVariable(mean, std) {
        override fun next(): Double {
            return clamp(average() - range, super.next(), average() + range)
        }
    }

    inner class DiscreetRandomVariable(private val numDice: Int, private val sidesPerDie: Int) : RandomVariable() {
        override fun next(): Double {
            var total = 0
            for (i in 0 until numDice) total += (nextInteger(sidesPerDie.toLong()) + 1).toInt()
            return total.toDouble()
        }

        override fun average(): Double {
            return numDice * sidesPerDie * .5
        }
    }

    inner class UniformRandomVariable(private val min: Double, private val max: Double) : RandomVariable() {
        override fun next(): Double {
            return min + nextReal() * (max - min)
        }

        override fun average(): Double {
            return (max + min) / 2
        }
    }

    companion object {
        private const val multiplier = 0x5DEECE66DL
        private const val addend = 0xBL
        private const val mask = (1L shl 48) - 1
        private const val DOUBLE_UNIT = 1.0 / (1L shl 53)
        private val seedUniquifier = AtomicLong(8682522807148012L)

        private fun seedUniquifier(): Long {
            // L'Ecuyer, "Tables of Linear Congruential Generators of
            // Different Sizes and Good Lattice Structure", 1999
            while (true) {
                val current = seedUniquifier.get()
                val next = current * 1181783497276652981L
                if (seedUniquifier.compareAndSet(current, next)) return next
            }
        }

        private fun initialScramble(seed: Long): Long {
            return seed xor multiplier and mask
        }
    }
}