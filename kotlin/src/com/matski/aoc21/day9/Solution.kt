package com.matski.aoc21.day9

import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.types.P
import com.matski.aoc21.shared.collection.extensions.getWithoutCare
import com.matski.aoc21.shared.collection.extensions.mutableList
import com.matski.aoc21.shared.collection.extensions.mutableSet
import com.matski.aoc21.shared.withMetrics
import mu.KLogger
import mu.KotlinLogging
import kotlin.streams.toList

private val maxListValue = listOf(Int.MAX_VALUE)
fun List<Int>.getWithoutCare(index: Int) = getWithoutCare(index, Int.MAX_VALUE)
fun List<List<Int>>.getWithoutCare(index: Int) = getWithoutCare(index, maxListValue)
fun List<List<Int>>.getValue(x: Int, y: Int) = getWithoutCare(y).getWithoutCare(x)
fun List<Int>.isMinimum(currentX: Int, currentY: Int, yData: List<List<Int>>): Boolean {
    return getWithoutCare(currentX - 1) > get(currentX)
            && getWithoutCare(currentX + 1) > get(currentX)
            && yData.getWithoutCare(currentY - 1).getWithoutCare(currentX) > get(currentX)
            && yData.getWithoutCare(currentY + 1).getWithoutCare(currentX) > get(currentX)
}


fun main() {
    val log = KotlinLogging.logger { }
    val data: List<List<Int>> = readFile("day9/input.txt") { s ->
        s.toCharArray().map(Char::toString).map(String::toInt)
    }
    var minCoords: MutableList<LocalMinimum> = mutableList()
    data.forEachIndexed { yIndex, xList ->
        xList.forEachIndexed { xIndex, value ->
            if (xList.isMinimum(xIndex, yIndex, data)) minCoords.add(LocalMinimum(xIndex, yIndex, value))
        }
    }
    log.info { "Min coords sum: ${minCoords.map { e -> e.value + 1 }.sum()}, all: $minCoords" }
    withMetrics {
        minCoords = minCoords.parallelStream().peek { e -> e.exploreBasin(data) }.toList().toMutableList()
        minCoords.sortByDescending(LocalMinimum::basinSize)
        log.info {
            "Sum of largest basins: ${
                minCoords.slice(0..2).map(LocalMinimum::basinSize).reduce { acc, i -> acc * i }
            }"
        }
    }
}

private data class LocalMinimum(val x: Int, val y: Int, val value: Int, var basinSize: Int = 0) {

    val debugLog : KLogger? = if (KotlinLogging.logger("LocalMinimum").isDebugEnabled) KotlinLogging.logger {  } else null

    fun exploreBasin(map: List<List<Int>>) {
        val visited = mutableSet<P<Int, Int>>()
        visit(this.x, this.y, visited, map)
        basinSize = visited.size
        debugLog?.debug { "LocalMinimum details: $this, visited: $visited" }
    }

    private fun visit(x: Int, y: Int, visited: MutableSet<P<Int, Int>>, map: List<List<Int>>) {
        visited.add(P(x, y))
        val toVisit = arrayOf(P(x - 1, y), P(x + 1, y), P(x, y - 1), P(x, y + 1))
        toVisit.forEach { searchLoc ->
            val testCoord = map.getValue(searchLoc.first, searchLoc.second)
            if (testCoord < 9 && testCoord > map.getValue(x, y) && !visited.contains(searchLoc)) {
                visit(searchLoc.first, searchLoc.second, visited, map)
            }
        }
    }
}
