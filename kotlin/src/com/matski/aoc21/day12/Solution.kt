package com.matski.aoc21.day12

import com.matski.aoc21.shared.collection.extensions.mutableList

fun main() {

}

data class Node(
    val caveName: String,
    val isLargeCave: Boolean = caveName.uppercase() == caveName,
    val neighbours: MutableList<Node> = mutableList()
)