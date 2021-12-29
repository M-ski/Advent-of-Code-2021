package com.matski.aoc21.shared.collection.extensions

fun <E> List<E>.getWithoutCare(index: Int, defaultValue: E) = if (index in this.indices) get(index) else defaultValue

fun <K, V> Map<K, V>.getNotNull(key: K): V = get(key) ?: throw IllegalArgumentException("$key not found in map")

fun <E> Collection<E>.nor(chars: Collection<E>): Collection<E> {
    val mutableChars = toMutableList()
    mutableChars.removeAll(chars)
    return mutableChars
}

fun <E> Collection<E>.filterAndGet(filterFn: (E) -> Boolean) = filter { e -> filterFn(e) }.single()

fun <E> Collection<E>.xand(another: Collection<E>): Collection<E> = filter { e -> another.contains(e) }