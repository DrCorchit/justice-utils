package com.drcorchit.justice.utils.math

import com.drcorchit.justice.utils.json.JsonUtils.GSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.*

class LatencyMonitor(val name: String) {
    private var size = 0
    private val latencies: TreeMap<Long, Int> = TreeMap<Long, Int>()

    fun size(): Int {
        return size
    }

    fun observeLatency(latency: Long) {
        val value: Int = latencies.getOrDefault(latency, 0) + 1
        size++
        latencies[latency] = value
    }

    fun computeStatistics(): MonitorResults {
        val medianIndex = size / 2
        val p95Index = (size * .95)
        val p99Index = (size * .99)
        var index = 0
        var total = 0L
        var median: Long = latencies.lastKey()
        var p95: Long = latencies.lastKey()
        var p99: Long = latencies.lastKey()
        for ((key, value) in latencies.entries) {
            if (value == 0) continue
            total += key * value
            index += value
            if (index >= medianIndex && index < medianIndex + value) median = key
            if (index >= p95Index && index < p95Index + value) p95 = key
            if (index >= p99Index && index < p99Index + value) p99 = key
        }
        return MonitorResults(size, total / size, median, p95, p99)
    }

    fun serialize(): JsonElement {
        return GSON.toJsonTree(computeStatistics())
    }

    data class MonitorResults(val size: Int, val mean: Long, val median: Long, val p95: Long, val p99: Long) {
        fun serialize(): JsonObject {
            return GSON.toJsonTree(this).asJsonObject
        }
    }
}