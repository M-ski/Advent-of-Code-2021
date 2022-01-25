package com.matski.aoc21.wordle

import mu.KLogger
import mu.KotlinLogging
import java.io.File

fun main() {
    WordleSolver(input = CommandLineWordleInput(), dictionary = WordsLookup().getWords()).solve()
}

class WordleSolver(
    val input: WordleInput,
    val dictionary: List<String>,
    private val filter: WordleFilter = WordleFilter(),
    private val log: KLogger = KotlinLogging.logger { },
    private var filteredWords: List<String> = dictionary,
    private var continueSolving: Boolean = true
) {

    fun solve() {
        while (continueSolving) {
            filteredWords = filteredWords.filter(filter::filterWord)
            val remainingWordCharFrequencies = filteredWords.flatMap { s -> s.toCharArray().toList() }
                .reduceToMap(0) { _, accumulatedValue -> accumulatedValue + 1 }.toMutableMap()
            val filteredScoredWords = getScoredFilteredWords(remainingWordCharFrequencies)
            // then lets log how many possible remaining words there are
            log.info {
                "Possible remaining words (length: ${filteredWords.size}): ${
                    if (filteredScoredWords.size > 5) filteredScoredWords.subList(0, 5) else filteredScoredWords
                }"
            }
            // and now, lets attempt to recommend words to guess by scoring future words based on what num of chars are
            // left to guess, then remove char scores for already known chars - no need to re-guess those
            val scoredWords = getScoredWordRecommendations(remainingWordCharFrequencies)
            // and show the remaining guesses
            val higestScoredWords = scoredWords.sortedByDescending { (_, freq) -> freq }
            log.info {
                "We recommend these guesses (total number of recommendations: ${higestScoredWords.size}), top: ${
                    if (higestScoredWords.size > 5) higestScoredWords.subList(0, 5) else higestScoredWords
                }"
            }
            log.info { filter }
            if (filteredWords.size <= 1) {
                continueSolving = false
            }
            // first, add a new set of input into the filter, then filter our dictionary
            filter.add(input.getInput())
        }
    }

    private fun getScoredFilteredWords(remainingWordCharFrequencies: Map<Char, Int>): List<Pair<String, Int>> {
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE
        remainingWordCharFrequencies.forEach { _, score ->
            min = if (score < min) score else min
            max = if (score > max) score else max
        }
        val normalizedCharFrequencies = remainingWordCharFrequencies.toMutableMap()
            .map { (c, score) ->
                val normalised = (score - min).toDouble() / (max - min).toDouble()
                Pair(c, (normalised * 100).toInt())
            }.toMap()
        return filteredWords
            .map { word ->
                var score = 0
                val seenLetters = mutableSetOf<Char>()
                word.forEachIndexed { i, c ->
                    if (c in filter.presentChars && c !in seenLetters) {
                        score += (if (charInCorrectPosition(i)) 2 else 1) * normalizedCharFrequencies.getOrDefault(c, 0)
                        seenLetters.add(c)
                    }
                    if (charInWrongPosition(c, i) || c in filter.nonPresentChars) {
                        score -= 1000
                    }
                }
                Pair(word, score)
            }.sortedByDescending { (_, freq) -> freq }
    }

    private fun getScoredWordRecommendations(remainingWordCharFrequencies: Map<Char, Int>): List<Pair<String, Int>> {
        val slimmedCharMap = remainingWordCharFrequencies.toMutableMap()
        filter.guessedChars.toCharArray().forEach { char -> slimmedCharMap.remove(char) }
        val scoredWords = dictionary
            .map { word ->
                // score the remaining words, do not reward words with multiple of the same char
                val seenLetters = mutableSetOf<Char>()
                var score = 0
                word.forEach { c ->
                    if (c in filter.nonPresentChars) {
                        score -= 1000
                    } else {
                        if ((c in seenLetters).not()) {
                            seenLetters.add(c)
                            score += slimmedCharMap.getOrDefault(c, 0)
                        } else {
                            score -= 1000
                        }
                    }
                }
                Pair(word, score)
            }
        return scoredWords
    }

    private fun charInCorrectPosition(i: Int) = filter.knownChars.any { (_, pos) -> pos == i }

    private fun charInWrongPosition(c: Char, i: Int) = filter.knownChars.any { (k, pos) -> c == k && pos != i }

}

data class WordleFilter(
    var knownChars: MutableList<Pair<Char, Int>> = mutableListOf(),
    var unkownChars: MutableList<Pair<Char, Int>> = mutableListOf(),
    var nonPresentChars: String = "",
    var presentChars: String = "",
    var guessedChars: String = ""
) {
    private fun refreshCaches() {
        // we want to know what words to filter out from future guesses - no point to re-guess a known char.
        guessedChars = "$nonPresentChars${
            knownChars.map { (c, _) -> c }.joinToString(separator = "")
        }${
            unkownChars.map { (c, _) -> c }.joinToString(separator = "")
        }"
        presentChars = "${knownChars.map(Pair<Char, Int>::first).joinToString(separator = "")}${
            unkownChars.map(Pair<Char, Int>::first).joinToString(separator = "")
        }"
    }

    fun filterWord(word: String): Boolean {
        var keep = true
        if (knownChars.isNotEmpty()) {
            for (charLocations in knownChars) {
                // if we found a char location, keep it!
                keep = keep && word[charLocations.second] == charLocations.first
            }
        }
        if (unkownChars.isNotEmpty()) {
            for (unknownCharLocations in unkownChars) {
                // filter out unknown words where the guessed character is in the word, but not at the position where it
                // was wrong to be at (ie, if e was in the word, but not a position 4, filter out words with e at pos 4)
                keep = keep
                        && word.contains(unknownCharLocations.first)
                        && word[unknownCharLocations.second] != unknownCharLocations.first
            }
        }
        if (nonPresentChars.isNotEmpty()) {
            for (char in word) {
                // filter out words which of course, do not contain guessed characters that were not in the word
                keep = keep && nonPresentChars.contains(char).not()
            }
        }
        return keep
    }

    fun add(newFilterSpec: WordleInput.In) {
        this.knownChars.addAll(newFilterSpec.knownChars)
        this.unkownChars.addAll(newFilterSpec.unkownChars)
        this.nonPresentChars += newFilterSpec.nonPresentChars
        this.refreshCaches()
    }
}

interface WordleInput {
    data class In(
        val knownChars: List<Pair<Char, Int>>,
        val unkownChars: List<Pair<Char, Int>>,
        val nonPresentChars: String
    )

    fun getInput(): In
}

class CommandLineWordleInput : WordleInput {
    var firstGuess = true
    val log = KotlinLogging.logger { }
    override fun getInput(): WordleInput.In {
        // give a hint to the user
        log.info { "Please input your guess results using the following format: x1n1,x2n2... :" }
        if (firstGuess) {
            log.info { "x is the guessed char, and n is a number representing: 1=present at location, 2=present in another locaiton, 3=not in word" }
            log.info { "eg, for the guess 'adieu', if a was in the correct position, and i somewhere else: a1,d3,i2,e3,u3" }
            firstGuess = false
        }
        // set up our wordle input vals
        val known = mutableList<Pair<Char, Int>>()
        val unKnown = mutableList<Pair<Char, Int>>()
        var nonPresent = ""
        // then request input, and iterate indexed over it to construct the input filter class
        readln().trim().split(",").forEachIndexed { i, guess ->
            val result = guess[1].toString().toInt()
            when (result) {
                1 -> known.add(Pair(guess[0], i))
                2 -> unKnown.add(Pair(guess[0], i))
                3 -> nonPresent += guess[0]
            }
        }
        return WordleInput.In(known, unKnown, nonPresent)
    }

}

class WordsLookup {
    fun getWords(): List<String> {
        return File(System.getProperty("user.dir") + "\\kotlin\\src\\com\\matski\\aoc21\\wordle\\src_extracted_wordle_words.txt")
            .readLines().distinct().toList()
    }
}

fun <E> mutableList(vararg elements: E): MutableList<E> = listOf(*elements).toMutableList()

fun <V, E> Collection<E>.reduceToMap(initialMapValue: V, reducer: (nextEntry: E, accumulatedValue: V) -> V): Map<E, V> {
    return reduceToMap(initialMapValue, { e -> e }, reducer)
}

fun <V, K, E> Collection<E>.reduceToMap(
    initialMapValue: V,
    keyMapper: (entry: E) -> K,
    reducer: (nextEntry: E, accumulatedValue: V) -> V
): Map<K, V> {
    val reduced = HashMap<K, V>()
    forEach { e ->
        val mapKey = keyMapper(e)
        reduced[mapKey] = reducer(e, reduced.getOrDefault(mapKey, initialMapValue))
    }
    return reduced
}