package com.matski.aoc21.day7

import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.withMetrics
import mu.KotlinLogging
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun main() {
    val log = KotlinLogging.logger { }
    withMetrics {
        val data: List<Int> = readFile("day7/input.txt") { s -> s.split(",").map(String::toInt) }[0]

        fun List<Int>.varianceSumFrom(int: Int, exponential: Boolean = true): Int {
            var varSum = 0
            if (exponential) {
                forEach { i -> varSum += (1 .. (max(i, int) - min(i, int))).sum() }
            } else {
                forEach { i -> varSum += abs(i - int) }
            }
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

        val minimumPosition = data.findMinVariance()
        withMetrics(identifier = "recursive search") {
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