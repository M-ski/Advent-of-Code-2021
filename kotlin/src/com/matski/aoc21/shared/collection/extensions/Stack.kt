package com.matski.aoc21.shared.collection.extensions

import java.util.*
import kotlin.collections.ArrayDeque

class Stack<E>(override val size: Int) : Collection<E> {
    private val internalList = ArrayDeque<E>(size)

    constructor() : this(0)

    /**
     * Pushes item to [Stack]
     * @param item Item to be pushed
     */
    fun push(item: E) = internalList.add(item)

    /**
     * Pops (removes and return) last item from [Stack]
     * @return item Last item if [Stack] is not empty, otherwise throws a [EmptyStackException]
     */
    fun pop(): E = if (isNotEmpty()) internalList.removeLast() else throw EmptyStackException()

    /**
     * Peeks (return) last item from [Stack]
     * @return item Last item if [Stack] is not empty, otherwise throws a [EmptyStackException]
     */
    fun peek(): E = if (isNotEmpty()) internalList[internalList.lastIndex] else throw EmptyStackException()

    /**
     * Map the elements from [Stack] in the natural order of the stack, each item is popped off the top of the stack
     * and then mapped using the specified mapper function
     * @return list of objects as defined by the mapper
     */
    inline fun <R> map(mapper: (E) -> R): List<R> {
        val mappedObjects = mutableList<R>()
        while (this.isNotEmpty()) {
            mappedObjects.add(mapper(this.pop()))
        }
        return mappedObjects
    }

    inline fun <R> reduce(initial: R, reducer: (acc: R, el: E) -> R): R {
        var final = initial
        while (this.isNotEmpty()) {
            final = reducer(initial, this.pop())
        }
        return final
    }

    override fun contains(element: E): Boolean = internalList.contains(element)

    override fun containsAll(elements: Collection<E>): Boolean = internalList.containsAll(elements)

    override fun isEmpty(): Boolean = internalList.isEmpty()

    override fun iterator(): Iterator<E> = internalList.iterator()

}

