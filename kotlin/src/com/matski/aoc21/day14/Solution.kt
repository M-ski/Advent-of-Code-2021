package com.matski.aoc21.day14

import com.matski.aoc21.shared.collection.extensions.add
import com.matski.aoc21.shared.collection.extensions.countOccurrences
import com.matski.aoc21.shared.collection.extensions.mutableList
import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.types.P
import mu.KotlinLogging

private const val simulationRuns = 40
private const val inputFile = "day14/input.txt"

fun main() {
    val log = KotlinLogging.logger {}
    val (startingPattern, mappings) = parseInput()
    var polymerGrowth = startingPattern.toCharArray().toMutableList()
    log.info { "Starting polymer: $startingPattern" }
    for (r in 1..simulationRuns) {
        val newPolymer = mutableList(polymerGrowth[0])
        for (i in 1 until polymerGrowth.size) {
            val mapping = "${polymerGrowth[i - 1]}${polymerGrowth[i]}"
            newPolymer.add(mappings[mapping])
            newPolymer.add(polymerGrowth[i])
        }
        log.debug { "Polymer after growth cycle $r: ${newPolymer.joinToString(separator = "")}" }
        log.info { "Occurences: (${newPolymer.countOccurrences().map { e -> "${e.key}:${e.value}" }.joinToString(separator = ", ")})" }
        polymerGrowth = newPolymer
    }
    val (minOccurance, maxOccurance) = getMinAndMaxCharOccurances(polymerGrowth.countOccurrences())
    log.info { "Max: $maxOccurance, Min: $minOccurance, Delta: ${maxOccurance.second - minOccurance.second}" }
}

private fun getMinAndMaxCharOccurances(occurences: Map<Char, Long>): Pair<P<Char, Long>, P<Char, Long>> {
    var minOccurance = P('a', Long.MAX_VALUE)
    var maxOccurance = P('a', Long.MIN_VALUE)
    occurences.forEach { entry ->
        if (entry.value > maxOccurance.second) {
            maxOccurance = P(entry.key, entry.value)
        }
        if (entry.value < minOccurance.second) {
            minOccurance = P(entry.key, entry.value)
        }
    }
    return Pair(minOccurance, maxOccurance)
}

fun parseInput(): P<String, Map<String, Char>> {
    var startingLine = ""
    val patterns: MutableMap<String, Char> = HashMap()
    readFile(inputFile) { line ->
        if (line.contains("->").not() && line.length > 1) {
            startingLine = line.trim()
        } else if (line.contains("->")) {
            val splitMapping = line.trim().split(" -> ").map { s -> s.trim() }
            patterns.put(splitMapping[0], splitMapping[1].first())
        }
    }
    return P(if (startingLine == "") throw IllegalArgumentException() else startingLine, HashMap(patterns))
}
