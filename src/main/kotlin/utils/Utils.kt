package utils

fun <K, V> Map<K, V>.filterIf(doFilter: Boolean, predicate: (Map.Entry<K, V>) -> Boolean): Map<K, V> {
    return if (doFilter) this.filter(predicate) else this
}

fun <V> Array<V>?.firstOr(or: () -> V) = this?.firstOrNull() ?: or()

fun <V> Set<V>.minusIfNotNull(element: V?) = if (element != null) this.minus(element) else this

fun <V> Collection<V>.firstOr(or: () -> V): V = if (this.isEmpty()) or() else this.first()
