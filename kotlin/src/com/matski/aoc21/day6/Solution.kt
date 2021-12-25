package com.matski.aoc21.day6

import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.withMetrics
import mu.KotlinLogging
import kotlin.streams.toList

private val log = KotlinLogging.logger { }
private val DAYS = 80

fun main() {

    withMetrics(logger = log) {
        var topLevelEelNumber = 0
        val eels: List<LanternEel> = readFile("day6/input.txt") { s ->
            s.split(",").map { num ->
                topLevelEelNumber += 1
                LanternEel(num.toInt(), topLevelEelNumber)
            }
        }[0]
        log.info { "Running simulation with ${eels.size} eels" }
        val allEels = eels.parallelStream().map(LanternEel::simulate).toList()
            .flatMap(LanternEel::getSelfAndChildren)
            .filter { eel -> eel.birthDate <= DAYS }
        log.debug { "All eels: $allEels" }
        StateAggregator(allEels).printState()
        log.info { "Simulation complete, produced ${allEels.size} eels" }
        StateAggregator(listOf(*(eels[0].getSelfAndChildren().toTypedArray()))).printState()
    }
}

private class StateAggregator(val eels: List<LanternEel>) {
    val eelStateOnDay = HashMap<Int, MutableList<Int>>().toMutableMap()

    init {
        for (day in 0..DAYS) {
            eelStateOnDay[day] = listOf<Int>().toMutableList()
        }
        eels.sortedWith { e1, e2 ->
            if (e1.birthDate == e2.birthDate) e1.topLevelEelNumber.compareTo(e2.topLevelEelNumber)
            else e1.birthDate.compareTo(e2.birthDate)
        }.forEach { eel ->
            eel.eelHistory.forEach { day, state ->
                eelStateOnDay[day]?.add(state)
            }
        }
    }

    fun printState() {
        log.info { "Printing eel state:" }
        eelStateOnDay.entries.forEach { entry ->
            val day = entry.key
            log.info { "Day $day: ${entry.value.joinToString(separator = ",")}" }
        }
    }
}

private data class LanternEel(var reproductiveCycleStage: Int, val topLevelEelNumber: Int, val birthDate: Int = 0) {

    private val children: MutableList<LanternEel> = emptyList<LanternEel>().toMutableList()
    val eelHistory: MutableMap<Int, Int> = HashMap()
    val eelLog = KotlinLogging.logger("Eel-$topLevelEelNumber-$birthDate")

    init {
        eelLog.info { "created new eel with initial reproductive state: $reproductiveCycleStage" }
    }

    fun getSelfAndChildren(): List<LanternEel> = when (children.size) {
        0 -> listOf(this)
        else -> listOf(this, *children.flatMap(LanternEel::getSelfAndChildren).toTypedArray())
    }

    fun simulate(): LanternEel {
        eelLog.debug { "Simulating for ${DAYS - birthDate} (started on $birthDate)" }
        var runDate = birthDate
        while (runDate <= DAYS) {
            eelLog.debug { "Simulating day $runDate, countdown to hornyness: $reproductiveCycleStage" }
            eelHistory[runDate] = reproductiveCycleStage
            when (reproductiveCycleStage) {
                0 -> {
                    eelLog.debug { "Producing baby" }
                    this.reproductiveCycleStage = 6
                    val babyEel = LanternEel(8, topLevelEelNumber, birthDate = runDate + 1)
                    children.add(babyEel)
                }
                else -> {
                    this.reproductiveCycleStage -= 1
                }
            }
            runDate += 1
        }
        children.forEach(LanternEel::simulate)
        return this
    }

    override fun toString(): String {
        return "LanternEel(eelNum=$topLevelEelNumber-$birthDate, reproductiveCycleStage=$reproductiveCycleStage, birthDate=$birthDate)"
    }
}