package com.matski.aoc21.day11

import com.matski.aoc21.shared.collection.extensions.mutableList
import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.withMetrics
import mu.KLogger
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs


fun main() {
    val log = KotlinLogging.logger { }
    var numFlashes = AtomicInteger(0)
    withMetrics(logger = log) {
        val dumboGrid =
            getDumbos(readFile("day11/input.txt") { s -> s.toCharArray().map(Char::digitToInt).toList() }, numFlashes)
        dumboGrid.registerNeighbours()
        // set up params we want to capture
        var allFlashed = false
        var day = 0
        val dumboGridSize = dumboGrid.flatten().size
        var numFlashesAtDay100 = 0
        // now simulate until all have flashed, or until at least day 100
        while (!allFlashed || day < 100) {
            day += 1
            log.debug { "Day: $day" }
            dumboGrid.flatten().forEach { dumbo -> dumbo.constructAdditionalPylons(true) }
            dumboGrid.flatten().forEach { dumbo -> dumbo.simulateFlash() }
            val numFlashedToday = dumboGrid.flatten().map(DumboOctopus::hasFlashed).filter { e -> e }.size
            log.debug {
                "Num flashed: $numFlashedToday, wanted: ${dumboGridSize}"
            }
            if (day == 100) numFlashesAtDay100 = numFlashes.get()
            allFlashed = numFlashedToday == dumboGridSize
        }
        log.info { "Day 100 num flashes: $numFlashesAtDay100" }
        log.info { "All flashed at day: $day" }
    }
}

private fun getDumbos(grid: List<List<Int>>, flashHolder: AtomicInteger): List<List<DumboOctopus>> {
    var y = grid.size
    val dumboGrid: List<List<DumboOctopus>> = grid.map { yLists ->
        var x = 1
        val dumbos = yLists.map { startingEnergy ->
            val octopus = DumboOctopus(x, y, startingEnergy, flashHolder)
            x += 1
            octopus
        }
        y -= 1
        dumbos
    }
    return dumboGrid
}

private fun List<List<DumboOctopus>>.registerNeighbours() {
    flatten().forEach { dumbo ->
        ((dumbo.y - 1)..(dumbo.y + 1)).toList().forEach { y ->
            this.getOrNull(this.size - y)?.forEach { e ->
                if (abs(dumbo.x - e.x) <= 1) {
                    dumbo.neighbours.add(e)
                }
            }
        }
        dumbo.neighbours.remove(dumbo)
    }
}

private fun List<List<DumboOctopus>>.printState(log: KLogger = KotlinLogging.logger { }, print: Boolean = false) {
    if (print) {
        var message = "State:\n"
        forEach { row ->
            message += "${
                row.map(DumboOctopus::energyLevel).map(Int::toString).joinToString(separator = " ")
            }\n"
        }
        log.info { message }
    }
}


data class DumboOctopus(
    val x: Int,
    val y: Int,
    var energyLevel: Int,
    val flashHolder: AtomicInteger,
    var hasFlashed: Boolean = false,
    val neighbours: MutableCollection<DumboOctopus> = mutableList()
) {

    fun constructAdditionalPylons(newDay: Boolean = false) {
        if (newDay) hasFlashed = false
        if (!hasFlashed) {
            energyLevel += 1
            if (!newDay) simulateFlash()
        }
    }

    fun simulateFlash() {
        if (energyLevel > 9) {
            hasFlashed = true
            flashHolder.incrementAndGet()
            energyLevel = 0
            neighbours.forEach(DumboOctopus::constructAdditionalPylons)
        }
    }

    override fun toString(): String {
        return "DumboOctopus(x=$x, y=$y, energyLevel=$energyLevel, hasFlashed=$hasFlashed, neighbours=[(${neighbours.size})${
            neighbours.map { d -> "(x=${d.x}, y=${d.y}, e=${d.energyLevel}, f=${d.hasFlashed})" }
                .joinToString(separator = ", ")
        }])"
    }


}