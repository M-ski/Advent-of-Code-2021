package com.matski.aoc21.day1

import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.withMetrics
import mu.KotlinLogging

fun main() {
    withMetrics {
        val log = KotlinLogging.logger { }
        var countIncreased = 0
        var count3DayIncreased = 0
        val inputData = readFile("day1/input.txt", { s -> s.toInt() })
        inputData.forEachIndexed { index, value ->
            if (index >= 1 && value > inputData[index - 1]) {
                countIncreased += 1
            }
            if (index >= 3 &&
                inputData.slice(index - 2..index).average()
                > inputData.slice(index - 3..index - 1).average()
            ) {
                count3DayIncreased += 1
            }
        }
        log.info { "Count increased = ${countIncreased}" }
        log.info { "Count increased (3 day moving average) = ${count3DayIncreased}" }
    }
}