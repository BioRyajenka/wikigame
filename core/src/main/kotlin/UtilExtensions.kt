
fun <E> MutableList<E>.mutableDropWhile(predicate: (E) -> Boolean) {
    while (isNotEmpty() && predicate(first())) removeFirst()
}
