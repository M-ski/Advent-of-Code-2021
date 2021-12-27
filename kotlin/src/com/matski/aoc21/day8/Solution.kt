package com.matski.aoc21.day8

import com.matski.aoc21.shared.readFile
import mu.KotlinLogging

fun main() {
    val log = KotlinLogging.logger {  }
    var lineNum = 0
    val displayInputs = readFile("day8/input.txt") { s ->
        lineNum+=1
        DisplayInput(s, lineNum)
    }
    log.info {"Part 1 only, numbers of 1/4/7/8: ${displayInputs.map(DisplayInput::encounteredNumbers).sum()}"}
}

fun MutableMap<Char, MutableSet<DS>>.addSegments(char: Char, segments: List<DS>) {
    val currentList = getOrDefault(char, emptySet<DS>().toMutableSet())
    currentList.addAll(segments)
    put(char, currentList)
}

private data class DisplayInput(val rawInput: String, val lineNum: Int) {

    private val dlog = KotlinLogging.logger("DisplayInput - line ${lineNum.toString().padStart(3, '0')}")
    private fun parseRaw(portion: Int) = rawInput.split(" | ")[portion].split(" ")
    val initialSegments: List<String> = parseRaw(0)
    val testSegments: List<String> = parseRaw(1)
    var encounteredNumbers = 0
    val decoderMap: Map<Char, DS> = decode(testSegments)

    private fun decode(segments: List<String>): Map<Char, DS> {
        val possiblePairings: MutableMap<Char, MutableSet<DS>> = HashMap()
        val encounteredKnownNumbers: MutableList<DN> = emptyList<DN>().toMutableList()
        val shortestSegmentsFirst = segments.sortedBy { s -> s.length }
        shortestSegmentsFirst.forEach { segment ->
            // switch over segment length initially, this helps constrain where certain display segments can be
            when (segment.length) {
                // 2 can only be the number 1
                2 -> addResolvableSegments(segment, possiblePairings, encounteredKnownNumbers, DN.ONE)
                // 3 can only be the number 7
                3 -> addResolvableSegments(segment, possiblePairings, encounteredKnownNumbers, DN.SEVEN)
                // 4 can only be 4
                4 -> addResolvableSegments(segment, possiblePairings, encounteredKnownNumbers, DN.FOUR)
                // 7 can only be 8
                7 -> addResolvableSegments(segment, possiblePairings, encounteredKnownNumbers, DN.EIGHT)
            }
        }
        dlog.info { "Encountered: $encounteredKnownNumbers" }
        encounteredNumbers+=encounteredKnownNumbers.size
        return emptyMap()
    }

    private fun addResolvableSegments(
        segment: String,
        possiblePairings: MutableMap<Char, MutableSet<DS>>,
        encounteredKnownNumbers: MutableList<DN>,
        dispalyNumber: DN
    ) {
        dlog.debug { "Processing num: $dispalyNumber" }
        var charsLeft = segment
        val segmentsLeft = dispalyNumber.listOFSegments.toMutableList()
        possiblePairings
            .filter { v -> v.value.any { e -> dispalyNumber.listOFSegments.contains(e) } }
            .forEach { e ->
                charsLeft = charsLeft.replaceFirst("${e.key}", "", true)
                segmentsLeft.removeAll(e.value)
            }
        charsLeft.forEach { c -> possiblePairings.addSegments(c, segmentsLeft) }
        encounteredKnownNumbers.add(dispalyNumber)
        dlog.debug { "Possible pairings now: $possiblePairings" }
    }

}

enum class DS {
    TOP, TOP_RIGHT, TOP_LEFT, MIDDLE, BOTTOM_RIGHT, BOTTOM_LEFT, BOTTOM;

    companion object {
        fun not(vararg segments: DS): Array<DS> {
            return values().filterNot { i -> segments.any { e -> e == i } }.toTypedArray()
        }
    }
}

enum class DN(vararg segments: DS) {
    ZERO(*DS.not(DS.MIDDLE)),
    ONE(DS.TOP_RIGHT, DS.BOTTOM_RIGHT),
    TWO(DS.TOP, DS.TOP_RIGHT, DS.MIDDLE, DS.BOTTOM_LEFT, DS.BOTTOM),
    THREE(DS.TOP, DS.TOP_RIGHT, DS.MIDDLE, DS.BOTTOM_RIGHT, DS.BOTTOM),
    FOUR(DS.TOP_LEFT, DS.MIDDLE, DS.TOP_RIGHT, DS.BOTTOM_RIGHT),
    FIVE(*DS.not(DS.BOTTOM_LEFT, DS.TOP_RIGHT)),
    SIX(*DS.not(DS.TOP_RIGHT)),
    SEVEN(DS.TOP, DS.TOP_RIGHT, DS.BOTTOM_RIGHT),
    EIGHT(*DS.not()),
    NINE(*DS.not(DS.BOTTOM_LEFT));
    val listOFSegments = segments.asList()
}