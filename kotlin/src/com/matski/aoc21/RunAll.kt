package com.matski.aoc21

import com.matski.aoc21.shared.withMetrics

fun main() {
    withMetrics(identifier = "All Solutions") {
        com.matski.aoc21.day1.main()
        com.matski.aoc21.day5.main()
//        com.matski.aoc21.day6.main()
        com.matski.aoc21.day7.main()
        com.matski.aoc21.day8.main()
        com.matski.aoc21.day9.main()
        com.matski.aoc21.day10.main()
        com.matski.aoc21.day11.main()
        com.matski.aoc21.day12.main()
    }
}