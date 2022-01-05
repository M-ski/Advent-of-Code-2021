package com.matski.aoc21.day13

import com.matski.aoc21.shared.collection.extensions.mutableList
import com.matski.aoc21.shared.collection.extensions.toPair
import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.types.P
import mu.KotlinLogging
import kotlin.math.max

fun main() {
    val log = KotlinLogging.logger { }
    var (instructions, coords) = getInput("input")
    log.info { instructions }
    log.info { coords }
    var foldNum = 0
    for (i in instructions) {
        foldNum+=1
        coords = fold(coords, i.first, i.second)
        log.info { "FoldNum [$foldNum] length: ${coords.size}" }
    }
    log.info { coords.print() }
}

private fun List<Pair<Int, Int>>.print(): String {
    val (maxX, maxY) = reduce { acc, e -> Pair(max(acc.first, e.first), max(acc.second, e.second)) }
    val stringBuilder = StringBuilder("State:\n")
    for (y in (0..maxY)) {
        for (x in 0..maxX) {
            stringBuilder.append(if (contains(P(x,y))) "#" else " ")
        }
        stringBuilder.append("\n")
    }
    return stringBuilder.toString()
}

private enum class FoldingDirection { X, Y }
private typealias F = FoldingDirection

private fun fold(points: List<Pair<Int, Int>>, on: F, foldOn: Int): List<Pair<Int, Int>> {
    return points.map { coord ->
        when (on) {
            F.X -> {
                Pair(if (coord.first > foldOn) foldOn - (coord.first - foldOn) else coord.first, coord.second)
            }
            F.Y -> {
                Pair(coord.first, if (coord.second > foldOn) foldOn - (coord.second - foldOn) else coord.second)
            }
        }
    }.toSet().toList()
}

private fun getInput(fileName: String): Pair<List<P<FoldingDirection, Int>>, List<P<Int, Int>>> {
    val instructions: MutableList<P<FoldingDirection, Int>> = mutableList()
    val initialCoords: MutableList<P<Int, Int>> = mutableList()
    readFile("day13/$fileName.txt").forEach { s ->
        if (s.startsWith("fold")) {
            instructions.add(s.replace("fold along ", "").trim().split("=")
                .toPair({e -> FoldingDirection.valueOf(e.uppercase())}, { e -> e.toInt() }))
        }
        if (s.contains(",")) {
            initialCoords.add(s.split(",").toPair({ e -> e.toInt() }, { e -> e.toInt() }))
        }
    }
    return Pair(instructions.toList(), initialCoords.toList())
}