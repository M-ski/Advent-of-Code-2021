package com.matski.aoc21.day12

import com.matski.aoc21.shared.collection.extensions.getNotNull
import com.matski.aoc21.shared.collection.extensions.mutableList
import com.matski.aoc21.shared.collection.extensions.mutableSet
import com.matski.aoc21.shared.readFile
import com.matski.aoc21.shared.types.P
import com.matski.aoc21.shared.withMetrics
import mu.KLogger
import mu.KotlinLogging

fun main() {
    val log = KotlinLogging.logger { }
    withMetrics(identifier = "Day 12") {
        val cavePairs = readFile("day12/input.txt") { s ->
            P(s.split("-")[0], s.split("-")[1])
        }
        log.info { "Edges: $cavePairs" }
        val graph = Graph.createFromRoot("start", cavePairs, Node::caveName)
        log.info { "Complied Cave Graph: $graph" }
        val paths = graph.getAllPaths()
        log.info { "All paths from start to end: \n${paths.joinToString(separator = "\n")}" }
        log.info { "That's ${paths.size} paths" }
    }
}

data class Graph(
    val edges: Collection<Pair<String, String>>,
    val indexingFunction: (Node) -> String,
) {
    lateinit var root: Node
    val indexedNodes: MutableMap<String, Node> = mutableMapOf()

    override fun toString(): String {
        return "Graph:\n${indexedNodes.map { entry -> entry.value.toString() }.joinToString(separator = "\n")}"
    }

    fun getAllPaths(): List<String> = navigate(
        indexedNodes.getNotNull("start"),
        indexedNodes.getNotNull("end")
    )

    fun navigate(
        from: Node,
        to: Node,
        visited: MutableList<String> = mutableList(),
        paths: MutableList<String> = mutableList()
    ): List<String> {
        visited.add(from.caveName)
        from.neighbours.forEach { node ->
            if (node == to) {
                paths.add("${visited.joinToString(",")},end")
            } else {
                if (node.isLargeCave){
                    navigate(node, to, mutableList(*visited.toTypedArray()), paths)
                } else if (!visited.contains(node.caveName)) {
                    navigate(node, to, mutableList(*visited.toTypedArray()), paths)
                }
            }
        }
        return paths
    }

    companion object {
        fun createFromRoot(
            rootNodeIndex: String,
            edges: Collection<Pair<String, String>>,
            indexingFunction: (Node) -> String = Node::caveName,
            log: KLogger = KotlinLogging.logger { }
        ): Graph {
            val graph = Graph(edges, indexingFunction)
            val rootNode = Node(rootNodeIndex, graph)
            graph.root = rootNode
            graph.indexedNodes[indexingFunction(rootNode)] = rootNode
            val caveNames = edges.flatMap { p -> listOf(p.first, p.second) }.toSet()
            var pass = 0
            while (graph.indexedNodes.size != caveNames.size) {
                pass += 1
                edges.forEach { p -> rootNode.addNode(p) }
                log.debug { "Pass $pass resolved: ${graph.indexedNodes.size}" }
            }
            // sorting the neighbours my name emulates the stuff in the hint
            graph.indexedNodes.values.forEach{node -> node.neighbours.sortBy(indexingFunction)}
            log.debug { "Compiled Graph: $this" }
            return graph
        }
    }
}

data class Node(
    val caveName: String,
    val parentGraph: Graph,
    val isLargeCave: Boolean = caveName.uppercase() == caveName,
    val neighbours: MutableList<Node> = mutableList()
) {
    fun addNode(cavePair: P<String, String>, visited: MutableSet<String> = mutableSet()) {
        // cave pair is a link between caveNames, so if either match this cave, add it as a neighbour if it is not already
        // there
        if (pairNotAlreadyAdded(cavePair)) {
            val cavesByName = parentGraph.indexedNodes
            if ((cavePair.first == caveName || cavePair.second == caveName)) {
                // get the new node name, and check if it already exists somwhere in the graph
                val newNodeName = if (cavePair.first == caveName) cavePair.second else cavePair.first
                val newNode = cavesByName.getOrPut(newNodeName) { Node(newNodeName, parentGraph) }
                // then assign it back into caves
                cavesByName[newNode.caveName] = newNode
                neighbours.add(newNode)
                newNode.neighbours.add(this)
                // search neighbours of new pairing, if they haven't been visited already
            } else if (!visited.contains(this.caveName)) {
                visited.add(this.caveName)
                neighbours.forEach { node -> node.addNode(cavePair, visited) }
            }
        }
    }

    private fun pairNotAlreadyAdded(cavePair: P<String, String>) = !neighbours.any { n ->
        (n.caveName == cavePair.first && cavePair.second == this.caveName) || (n.caveName == cavePair.second && cavePair.first == this.caveName)
    }

    override fun toString(): String {
        return "Node(caveName='$caveName', isLargeCave=$isLargeCave, neighbours=${neighbours.map { n -> n.caveName }})"
    }

}