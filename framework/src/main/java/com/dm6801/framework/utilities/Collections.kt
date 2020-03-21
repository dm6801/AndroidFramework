package com.dm6801.framework.utilities

fun <E> MutableSet<E>.replace(element: E) {
    remove(element)
    add(element)
}

fun <E> MutableSet<E>.replaceAll(elements: Collection<E>) {
    removeAll(elements)
    addAll(elements)
}