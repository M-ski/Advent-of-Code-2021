package com.matski.aoc21.day14

import com.matski.aoc21.shared.collection.extensions.reduceToMap
import com.matski.aoc21.shared.types.P
import mu.KotlinLogging

private const val simulationRuns = 10

fun main() {
    val log = KotlinLogging.logger {}
    val (startingPattern, mappings) = parseInput()
    var polymerGrowth = startingPattern.toCharArray().toMutableList()
    var polymerGrowthMappings = HashMap<String, Long>().toMutableMap()
    for (i in 1..polymerGrowth.size - 1) {
        val key = "${polymerGrowth[i - 1]}${polymerGrowth[i]}"
        polymerGrowthMappings[key] = polymerGrowthMappings.getOrDefault(key, 0L) + 1
    }
    log.info { "Starting polymer: $startingPattern" }
    log.info { "Mappings: $mappings" }
    log.info { "Mapping occurrances: $polymerGrowthMappings" }
    for (r in 1..simulationRuns) {
        val newPolymer = HashMap<String, Long>()
        for (mapping in polymerGrowthMappings.keys) {
            if (mapping in mappings) {
                val newChar = mappings[mapping]
                arrayOf("${mapping[0]}$newChar", "$newChar${mapping[1]}").forEach { key ->
                    newPolymer[key] = newPolymer.getOrDefault(key, 0L) + polymerGrowthMappings.getOrDefault(mapping, 1L)
                }
            }
        }
        log.info { "Occurances after run: $r - $newPolymer" }
        polymerGrowthMappings = newPolymer
    }
    val (minOccurance, maxOccurance) = getMinAndMaxCharOccurances(polymerGrowthMappings)
    log.info { "Max: $maxOccurance, Min: $minOccurance, Delta: ${maxOccurance.second - minOccurance.second}" }
}

private fun getMinAndMaxCharOccurances(occurences: MutableMap<String, Long>): Pair<P<Char, Long>, P<Char, Long>> {
    var minOccurance = P('a', Long.MAX_VALUE)
    var maxOccurance = P('a', Long.MIN_VALUE)
    occurences
        .flatMap { e -> listOf(P(e.key[0], e.value), P(e.key[1], e.value)) }
        .reduceToMap(0L, { e -> e.first }) { nextEntry, accumulatedValue -> accumulatedValue + nextEntry.second }
        .entries
        .forEach { entry ->
            if (entry.value > maxOccurance.second) {
                maxOccurance = P(entry.key, entry.value)
            }
            if (entry.value < minOccurance.second) {
                minOccurance = P(entry.key, entry.value)
            }
        }
    return Pair(minOccurance, maxOccurance)
}
