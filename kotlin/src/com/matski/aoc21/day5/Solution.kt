package com.matski.aoc21.day5

import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.withMetrics
import mu.KotlinLogging

val log = KotlinLogging.logger { }

fun main() {
    withMetrics {
        val lineDefinitions: List<LineDefinition> = readFile("d5-input.txt") { s ->
            val coordinates = s.split("->")
                .flatMap { e -> e.split(",") }
                .map { r -> r.trim().toInt() }.toIntArray()
            LineDefinition(*coordinates)
        }
        log.debug { "read ${lineDefinitions.size} line definitions" }
        val groupedLineCoordinates: MutableMap<Pair<Int, Int>, Int> = HashMap<Pair<Int, Int>, Int>().toMutableMap()
        lineDefinitions.flatMap(LineDefinition::interpolateCoords).forEach { pair ->
            groupedLineCoordinates[pair] = groupedLineCoordinates.getOrDefault(pair, 0) + 1
        }
        log.info {
            val map =
                groupedLineCoordinates.values.groupBy { e -> e }.map { entry -> Pair(entry.key, entry.value.size) }
            var value = 0
            map.forEach { pair -> if (pair.first > 1) value += pair.second }
            "Topology: $map, num greater than 1: ${value}"
        }
    }
}

private enum class Direction { Vertical, Horizontal, Diagonal }

private data class LineDefinition(val startX: Int, val startY: Int, val endX: Int, val endY: Int) {
    constructor(vararg inputs: Int) : this(inputs[0], inputs[1], inputs[2], inputs[3])

    val direction: Direction = run {
        if (startX != endX && startY == endY) return@run Direction.Horizontal
        if (startY != endY && startX == endX) return@run Direction.Vertical
        return@run Direction.Diagonal
    }

    fun interpolateCoords(): List<Pair<Int, Int>> {
        when (direction) {
            Direction.Horizontal -> return range(Coord.X).map { e -> Pair(e, startY) }
            Direction.Vertical -> return range(Coord.Y).toList().map { e -> Pair(startX, e) }
            Direction.Diagonal -> return range(Coord.X).zip(range(Coord.Y))
        }
    }

    private enum class Coord { X, Y }
    private fun range(coord: Coord): List<Int> {
        val coordPair = if (Coord.X == coord) Pair(startX, endX) else Pair(startY, endY)
        if (coordPair.first < coordPair.second) {
            return (coordPair.first..coordPair.second).toList()
        } else {
            return (coordPair.first downTo coordPair.second).toList()
        }
    }
}