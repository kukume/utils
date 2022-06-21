@file:Suppress("unused")

package me.kuku.utils

import java.util.*

object Lists {
    private val random = Random()

    @JvmStatic
    fun <T> newArrayList(vararg t: T): List<T> {
        return ArrayList(listOf(*t))
    }

    @JvmStatic
    fun <T> random(list: List<T>): T? {
        val size = list.size
        return if (size == 0) null else list[random.nextInt(size)]
    }

    @JvmStatic
    fun <E> of(vararg e: E): MutableList<E> {
        return mutableListOf(*e)
    }

    @JvmStatic
    fun <E> of(e1: E): MutableList<E> {
        return mutableListOf(e1)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E): MutableList<E> {
        return mutableListOf(e1, e2)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E): MutableList<E> {
        return mutableListOf(e1, e2, e3)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6, e7)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6, e7, e8)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E, e9: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6, e7, e8, e9)
    }

    @JvmStatic
    fun <E> of(e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E, e9: E, e10: E): MutableList<E> {
        return mutableListOf(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10)
    }
}