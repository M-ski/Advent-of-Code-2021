package com.matski.aoc21.day8

import com.matski.aoc21.shared.collection.extensions.filterAndGet
import com.matski.aoc21.shared.collection.extensions.getNotNull
import com.matski.aoc21.shared.collection.extensions.nor
import com.matski.aoc21.shared.collection.extensions.xand
import com.matski.aoc21.shared.readFile
import mu.KotlinLogging

fun MutableMap<Char, MutableSet<DS>>.getConcretePairedKeys(): Collection<Char> =
    entries.filter { e -> e.value.size == 1 }.map { e -> e.key }

fun MutableMap<Char, MutableSet<DS>>.addSegments(char: Char, segments: List<DS>) {
    val currentList = getOrDefault(char, emptySet<DS>().toMutableSet())
    currentList.addAll(segments)
    put(char, currentList)
}

fun MutableMap<Char, MutableSet<DS>>.assignSegment(char: Char, segment: DS) {
    forEach { _, segementSet -> segementSet.remove(segment) }
    put(char, mutableSetOf(segment))
}

fun String.nor(chars: Collection<Char>): Collection<Char> = toCharArray().toList().nor(chars)


fun main() {
    val log = KotlinLogging.logger { }
    var lineNum = 0
    val incAndGetLineNum = { lineNum += 1; lineNum }
    val displayInputs = readFile("day8/input.txt") { s -> DisplayInput(s, incAndGetLineNum()) }
    log.info { "Sum: ${displayInputs.map { e -> e.decode() }.sum()}" }
}

private data class DisplayInput(val rawInput: String, val lineNum: Int) {
    private val dlog = KotlinLogging.logger("DisplayInput - line ${lineNum.toString().padStart(3, '0')}")
    private fun parseRaw(portion: Int) = rawInput.split(" | ")[portion].split(" ")
    val initialSegments: List<String> = parseRaw(0)
    val testSegments: List<String> = parseRaw(1)
    val decoderMap: Map<Char, DS> = decode(initialSegments)

    fun decode(): Int {
        val sb = StringBuilder()
        testSegments.map { s ->
            s.map { e ->
                decoderMap.get(e) ?: throw IllegalArgumentException("$e not found in decoder map")
            }
        }.map { e -> DN.from(e).toString() }.forEach { e -> sb.append(e) }
        val decodedNum = sb.toString().toInt()
        dlog.debug { "Decoded Number: $decodedNum, mappings: $decoderMap" }
        return decodedNum
    }

    private fun decode(segments: List<String>): Map<Char, DS> {
        val possiblePairings: MutableMap<Char, MutableSet<DS>> = HashMap()
        val encounteredKnownNumbers: MutableMap<DN, Set<Char>> = HashMap()
        val shortestSegmentsFirst = segments.sortedBy { s -> s.length }
        resolveUniqueNumbers(shortestSegmentsFirst, possiblePairings, encounteredKnownNumbers)
        resolveNumberThree(
            shortestSegmentsFirst.filterAndGet { s ->
                s.length == 5 && s.toCharArray().toList().containsAll(encounteredKnownNumbers.getNotNull(DN.SEVEN))
            },
            possiblePairings,
            encounteredKnownNumbers
        )
        resolveViaNumberSix(
            shortestSegmentsFirst.filterAndGet { s ->
                s.length == 6 && s.toCharArray().toList().containsAll(encounteredKnownNumbers.getNotNull(DN.ONE)).not()
            },
            possiblePairings,
            encounteredKnownNumbers
        )
        return mapOf(*possiblePairings.entries.map { e -> Pair(e.key, e.value.single()) }.toTypedArray())
    }

    private fun resolveViaNumberSix(
        segment: String,
        possiblePairings: MutableMap<Char, MutableSet<DS>>,
        encounteredKnownNumbers: MutableMap<DN, Set<Char>>
    ) {
        encounteredKnownNumbers.put(DN.SIX, segment.toCharArray().toSet())
        val topRight = ('a'..'g').toList()
            .nor(encounteredKnownNumbers.getNotNull(DN.SIX))
            .nor(possiblePairings.getConcretePairedKeys()).single()
        dlog.debug { "Resolve six: segment: $segment, topRight=$topRight" }
        possiblePairings.assignSegment(topRight, DS.TOP_RIGHT)
        dlog.info { "Pairings: $possiblePairings" }
    }

    private fun resolveNumberThree(
        segment: String,
        possiblePairings: MutableMap<Char, MutableSet<DS>>,
        encounteredKnownNumbers: MutableMap<DN, Set<Char>>
    ) {
        // logic here - the deltas between 3 - 7 & 4 - 7 can resolve the bottom, middle and top_left segments: using nor or exclusive and
        val deltaThree = segment.nor(encounteredKnownNumbers.getNotNull(DN.SEVEN))
        val deltaFour = encounteredKnownNumbers.getNotNull(DN.FOUR).nor(encounteredKnownNumbers.getNotNull(DN.SEVEN))
        val bottomChar = deltaThree.nor(deltaFour).single()
        val middleChar = deltaThree.xand(deltaFour).single()
        val topLeft = deltaFour.nor(deltaThree).single()
        dlog.debug { "Resolve three: segment: $segment, deltaThree: $deltaThree, deltaFour: $deltaFour, bottomChar: $bottomChar, middleChar: $middleChar, topLeft: $topLeft" }
        possiblePairings.assignSegment(bottomChar, DS.BOTTOM)
        possiblePairings.assignSegment(middleChar, DS.MIDDLE)
        possiblePairings.assignSegment(topLeft, DS.TOP_LEFT)
        encounteredKnownNumbers[DN.THREE] = segment.toCharArray().toSet()
        dlog.debug { "Possible pairings now: $possiblePairings" }
    }

    private fun resolveUniqueNumbers(
        shortestSegmentsFirst: List<String>,
        possiblePairings: MutableMap<Char, MutableSet<DS>>,
        encounteredKnownNumbers: MutableMap<DN, Set<Char>>
    ) {
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
        dlog.info { "Encountered: ${encounteredKnownNumbers.keys}" }
    }

    private fun addResolvableSegments(
        segment: String,
        possiblePairings: MutableMap<Char, MutableSet<DS>>,
        encounteredKnownNumbers: MutableMap<DN, Set<Char>>,
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
        encounteredKnownNumbers[dispalyNumber] = segment.toCharArray().toSet()
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

enum class DN(val realNum: Int, vararg segments: DS) {
    ZERO(0, *DS.not(DS.MIDDLE)),
    ONE(1, DS.TOP_RIGHT, DS.BOTTOM_RIGHT),
    TWO(2, DS.TOP, DS.TOP_RIGHT, DS.MIDDLE, DS.BOTTOM_LEFT, DS.BOTTOM),
    THREE(3, DS.TOP, DS.TOP_RIGHT, DS.MIDDLE, DS.BOTTOM_RIGHT, DS.BOTTOM),
    FOUR(4, DS.TOP_LEFT, DS.MIDDLE, DS.TOP_RIGHT, DS.BOTTOM_RIGHT),
    FIVE(5, *DS.not(DS.BOTTOM_LEFT, DS.TOP_RIGHT)),
    SIX(6, *DS.not(DS.TOP_RIGHT)),
    SEVEN(7, DS.TOP, DS.TOP_RIGHT, DS.BOTTOM_RIGHT),
    EIGHT(8, *DS.not()),
    NINE(9, *DS.not(DS.BOTTOM_LEFT));

    val listOFSegments = segments.asList()

    companion object {
        fun from(segments: List<DS>): Int =
            values().filter { e -> e.listOFSegments.containsAll(segments) && e.listOFSegments.size == segments.size }
                .single().realNum
    }
}
