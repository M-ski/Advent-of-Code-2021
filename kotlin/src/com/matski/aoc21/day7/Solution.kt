package com.matski.aoc21.day7

import com.matski.aoc21.shared.withInput
import com.matski.aoc21.shared.withMetrics
import mu.KLogger
import mu.KotlinLogging
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


fun List<Int>.varianceSumFrom(int: Int, exponential: Boolean = true): Int {
    var varSum = 0
    if (exponential) {
        forEach { i -> varSum += (1..(max(i, int) - min(i, int))).sum() }
    } else {
        forEach { i -> varSum += abs(i - int) }
    }
    return varSum
}

tailrec fun List<Int>.findMinVariance(
    startValue: Int = this.minOrNull() ?: 0,
    endValue: Int = this.maxOrNull() ?: 1000,
    log: KLogger = KotlinLogging.logger("")
): Int {
    if (startValue == endValue) return startValue
    val splitValue = (startValue + endValue) / 2
    val startSum = varianceSumFrom(startValue)
    val splitSum = varianceSumFrom(splitValue)
    val endSum = varianceSumFrom(endValue)
    log.debug { "MinVariance: startValue:$startValue, endValue:$endValue, startVar:$startSum, splitVar:$splitSum, endVar:$endSum" }
    if (startSum <= splitSum && splitSum <= endSum) {
        return findMinVariance(startValue, splitValue, log)
    } else if (splitSum <= endSum && startSum >= splitSum) {
        return findMinVariance(startValue + 1, endValue, log)
    } else {
        return findMinVariance(splitValue, endValue, log)
    }
}

fun main() {
    val log = KotlinLogging.logger { }
    withInput("day7/input.txt") { s ->
        val data: List<Int> = s.split(",").map(String::toInt)

        withMetrics(identifier = "recursive search") {
            val minimumPosition = data.findMinVariance()
            log.info { "found by min $minimumPosition -> total fuel: ${data.varianceSumFrom(minimumPosition)}" }
        }

        withMetrics(identifier = "brute force") {
            log.info {
                "brute force method: ${
                    ((data.minOrNull() ?: 0)..(data.maxOrNull() ?: 10000)).map { i -> data.varianceSumFrom(i) }
                        .minOrNull()
                }"
            }
        }

    }

}