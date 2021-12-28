package com.matski.aoc21.day9

import com.matski.aoc21.shared.readFile
import mu.KotlinLogging

private val maxListValue = listOf(Int.MAX_VALUE)
fun <E> List<E>.getWithoutCare(index: Int, defaultValue: E) = if (index in this.indices) get(index) else defaultValue
fun List<Int>.getWithoutCare(index: Int) = if (index in this.indices) get(index) else Int.MAX_VALUE
fun List<Int>.isMinimum(currentX: Int, currentY: Int, yData: List<List<Int>>): Boolean {
    return getWithoutCare(currentX - 1) > get(currentX)
            && getWithoutCare(currentX + 1) > get(currentX)
            && yData.getWithoutCare(currentY - 1, maxListValue).getWithoutCare(currentX) > get(currentX)
            && yData.getWithoutCare(currentY + 1, maxListValue).getWithoutCare(currentX) > get(currentX)
}

fun main() {
    val log = KotlinLogging.logger { }
    val data: List<List<Int>> = readFile("day9/input.txt") { s ->
        s.toCharArray().map(Char::toString).map(String::toInt)
    }
    val minCoords: MutableList<SimpleCoord> = emptyList<SimpleCoord>().toMutableList()
    data.forEachIndexed { yIndex, xList ->
        xList.forEachIndexed { xIndex, value ->
            if (xList.isMinimum(xIndex, yIndex, data)) minCoords.add(SimpleCoord(xIndex, yIndex, value))
        }
    }
    log.info { "Min coords sum: ${minCoords.map { e -> e.value + 1 }.sum()}, all: $minCoords" }
}

private data class SimpleCoord(val x: Int, val y: Int, val value: Int)
