package com.matski.aoc21.day6

import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.withMetrics
import mu.KotlinLogging


fun main() {
    val days = 256
    val log = KotlinLogging.logger { }
    withMetrics {
        val eels: List<Int> = readFile("day6/input.txt") { s -> s.split(",") }
            .flatMap { s -> s.map(String::toInt) }
        // instead of modelling each eel, we can model on what day of the week they create an offspring, when we do,
        // just add 1 to that day of the week's placeholder value (and as we have multiple per day, add that day's
        // count to that position
        var birthdays = LongArray(9).toMutableList()
        eels.forEach { eel -> birthdays[eel] += 1L }

        for (n in 1..days) {
            // first move everything left to simulate a "day", this also simulates new offspring being created
            birthdays.shiftLeftInPlace()
            // add the "parents" from the last position into position 6, this replicates the normal breeding cycle
            // after the initial 8-day breeding warmup
            birthdays[6] += birthdays[8]
            log.info { "Day ${n} Sum: ${birthdays.sum()}, list: $birthdays" }
        }

    }
}

private inline fun <reified E> MutableList<E>.shiftLeftInPlace() {
    val leftmost = this.removeFirst()
    val rest = this.toMutableList()
    rest.add(leftmost)
    this.clear()
    this.addAll(rest)
}
