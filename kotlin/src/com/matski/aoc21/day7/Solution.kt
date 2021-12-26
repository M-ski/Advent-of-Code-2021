package com.matski.aoc21.day7

import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.withMetrics
import mu.KotlinLogging
import kotlin.math.abs

fun main() {
    val log = KotlinLogging.logger { }
    withMetrics {
        val data: List<Int> = readFile("day7/input.txt") { s -> s.split(",").map(String::toInt) }[0]
        fun List<Int>.varianceSumFrom(int: Int): Int {
            var varSum = 0
            forEach { i -> varSum = varSum + abs(i - int) }
            return varSum
        }

        tailrec fun List<Int>.findMinVariance(
            startValue: Int = this.minOrNull() ?: 0,
            endValue: Int = this.maxOrNull() ?: 1000
        ): Int {
            if (startValue == endValue) return startValue
            val splitValue = (startValue + endValue) / 2
            val startSum = varianceSumFrom(startValue)
            val splitSum = varianceSumFrom(splitValue)
            val endSum = varianceSumFrom(endValue)
            log.debug { "MinVariance: startValue:$startValue, endValue:$endValue, startVar:$startSum, splitVar:$splitSum, endVar:$endSum" }
            if (startSum <= splitSum && splitSum <= endSum) {
                return findMinVariance(startValue, splitValue)
            } else if (splitSum <= endSum && startSum >= splitSum) {
                return findMinVariance(startValue + 1, endValue)
            } else {
                return findMinVariance(splitValue, endValue)
            }
        }

        log.info { "found by min ${data.findMinVariance()}" }
        (0..10).forEach { v -> log.info { "For '$v' variance: ${data.varianceSumFrom(v)}" } }
    }

}