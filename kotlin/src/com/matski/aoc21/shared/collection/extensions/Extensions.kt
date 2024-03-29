package com.matski.aoc21.shared.collection.extensions

fun <E> mutableList(vararg elements: E): MutableList<E> = listOf(*elements).toMutableList()
fun <E> mutableSet(vararg elements: E): MutableSet<E> = setOf(*elements).toMutableSet()

fun <E> List<E>.getWithoutCare(index: Int, defaultValue: E) = if (index in this.indices) get(index) else defaultValue

fun <K, V> Map<K, V>.getNotNull(key: K): V = get(key) ?: throw IllegalArgumentException("$key not found in map")

fun <E> Collection<E>.nor(chars: Collection<E>): Collection<E> {
    val mutableChars = toMutableList()
    mutableChars.removeAll(chars)
    return mutableChars
}

fun <E> Collection<E>.filterAndGet(filterFn: (E) -> Boolean) = single { e -> filterFn(e) }

fun <E> Collection<E>.xand(another: Collection<E>): Collection<E> = filter { e -> another.contains(e) }

fun <E> MutableCollection<E>.add(element: E?) {
    if (element != null) add(element)
}

fun <L, R, E> Collection<E>.toPair(lMapper: (E) -> L, rMapper: (E) -> R): Pair<L, R> {
    if (size != 2) throw IllegalArgumentException("toPair(): expected collection not containing 2 entries, found: $size")
    return Pair(lMapper(elementAt(0)), rMapper(elementAt(1)))
}
fun <E, M> Collection<E>.toPair(valueMapper: (E) -> M): Pair<E, M> = toPair({s -> s}, valueMapper)
fun <E> Collection<E>.toPair(): Pair<E, E> = toPair({ s -> s })

fun <V, E> Collection<E>.reduceToMap(initialMapValue: V, reducer: (nextEntry: E, accumulatedValue: V) -> V): Map<E, V> {
    return reduceToMap(initialMapValue, {e -> e}, reducer)
}

fun <V, K, E> Collection<E>.reduceToMap(initialMapValue: V, keyMapper: (entry: E) -> K, reducer: (nextEntry: E, accumulatedValue: V) -> V): Map<K, V> {
    val reduced = HashMap<K, V>()
    forEach { e ->
        val mapKey = keyMapper(e)
        reduced[mapKey] = reducer(e, reduced.getOrDefault(mapKey, initialMapValue))
    }
    return reduced
}

fun <E> Collection<E>.countOccurrences(): Map<E, Long> {
    return reduceToMap(0) { _, acc -> acc + 1 }
}
