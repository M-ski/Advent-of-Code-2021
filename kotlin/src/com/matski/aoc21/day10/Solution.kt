package com.matski.aoc21.day10

import com.matski.aoc21.shared.collection.extensions.Stack
import com.matski.aoc21.shared.collection.extensions.getNotNull
import com.matski.aoc21.shared.types.P
import com.matski.aoc21.shared.readFile
import mu.KotlinLogging

private val charPairings = mapOf(P('<', '>'), P('(', ')'), P('{', '}'), P('[', ']'))
private val reverseCharPairings = charPairings.map { e -> P(e.value, e.key) }.toMap()
private val scoreMap = mapOf(P(')', 3), P(']', 57), P('}', 1197), P('>', 25137))
private val completionScoreMap = mapOf(P(')', 1), P(']', 2), P('}', 3), P('>', 4))

fun main() {
    val log = KotlinLogging.logger { }
    val data = readFile("day10/input.txt") { s -> s.toCharArray() }
    // part 1
    val corruptedLineEndChars = data.map(::parseLine)
    val part1sum = corruptedLineEndChars
        .filter { e -> e.first == ParseStatus.ERROR }
        .sumOf { e -> scoreMap.getNotNull(e.second.first()) }
    log.info { "corr: $corruptedLineEndChars, score: $part1sum" }
    // part 2
    val part2Scores = corruptedLineEndChars
        .filter { e -> e.first == ParseStatus.INCOMPLETE }
        .map { e -> calcCompletionScore(e.second) }
        .sorted()
    log.debug { "all scores: $part2Scores" }
    log.info { "middle score: ${part2Scores[(part2Scores.size / 2)]}" }
}

private fun parseLine(splitLine: CharArray): Pair<ParseStatus, List<Char>> {
    val openedChars = Stack<Char>()
    splitLine.forEach { char ->
        if (char in charPairings.keys) {
            openedChars.push(char)
        } else {
            val last = openedChars.pop()
            if (last != reverseCharPairings.getNotNull(char)) return Pair(ParseStatus.ERROR, listOf(char))
        }
    }
    return Pair(ParseStatus.INCOMPLETE, openedChars.map { e -> charPairings.getNotNull(e) }.toList())
}

private fun calcCompletionScore(completionSegments: List<Char>): Long {
    return completionSegments.map(completionScoreMap::getNotNull).map(Int::toLong)
        .reduce { acc, i -> (acc * 5) + i }
}

private enum class ParseStatus { ERROR, INCOMPLETE }