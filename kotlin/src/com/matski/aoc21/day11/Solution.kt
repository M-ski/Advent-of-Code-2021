package com.matski.aoc21.day11

import com.matski.aoc21.shared.readFile
import mu.KLogger
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs


fun main() {
    val log = KotlinLogging.logger { }
    var numFlashes = AtomicInteger(0)
    val dumboGrid: List<List<DumboOctopus>> = getDumbos(
        readFile("day11/input.txt") { s ->
            s.toCharArray().map(Char::digitToInt).toList()
        }, numFlashes)
    dumboGrid.registerNeighbours()
    log.info { dumboGrid[3][3] }
    log.info { "Starting state:" }
    dumboGrid.printState(log, true)
    (1..100).forEach { day ->
        log.info { "Day: $day" }
        dumboGrid.flatten().forEach { dumbo -> dumbo.constructAdditionalPylons(true) }
        dumboGrid.flatten().forEach{ dumbo -> dumbo.simulateFlash()}
        dumboGrid.printState(log, true)
    }
    log.info { "num flashes: ${numFlashes.get()}" }
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
    val neighbours: MutableCollection<DumboOctopus> = emptyList<DumboOctopus>().toMutableList()
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