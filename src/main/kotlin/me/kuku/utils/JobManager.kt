@file:Suppress("unused")

package me.kuku.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object JobManager {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @JvmStatic
    fun now(runnable: Runnable): Job {
        return coroutineScope.launch {
            try {
                runnable.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun now(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch {
            kotlin.runCatching {
                block()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun delay(wait: Long, runnable: Runnable): Job {
        return coroutineScope.launch {
            kotlinx.coroutines.delay(wait)
            try {
                runnable.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun delay(wait: Long, block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch {
            kotlinx.coroutines.delay(wait)
            kotlin.runCatching {
                block()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun every(wait: Long = 0, runnable: Runnable): Job {
        return coroutineScope.launch {
            while (true) {
                kotlinx.coroutines.delay(wait)
                try {
                    runnable.run()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun every(wait: Long = 0, block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch {
            while (true) {
                kotlinx.coroutines.delay(wait)
                kotlin.runCatching {
                    block()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    private fun firstDelay(atTime: String): Long {
        val sss = atTime.split(" ")
        val time = if (sss.size == 1) sss[0] else sss[1]
        val arr = time.split(":")
        var str = when (arr.size) {
            1 -> "$time:00:00"
            2 -> "$time:00"
            else -> time
        }
        str = if (sss.size == 1) {
            val nowStrTime = DateTimeFormatterUtils.formatNow("yyyy-MM-dd ")
            nowStrTime + str
        } else "${sss[0]} $str"
        val todayCronTime = DateTimeFormatterUtils.parse(str, "yyyy-MM-dd HH:mm:ss")
        val nowTime = System.currentTimeMillis()
        return if (todayCronTime > nowTime) todayCronTime - nowTime
        else todayCronTime + 1000 * 60 * 60 * 24 - nowTime
    }

    // 2022-03-02 11:33:22   || 11:33:22
    fun atTime(atTime: String, always: Boolean = false, block: suspend CoroutineScope.() -> Unit): Job {
        val firstDelay = firstDelay(atTime)
        return coroutineScope.launch {
            if (always) {
                kotlinx.coroutines.delay(firstDelay)
                while (true) {
                    kotlin.runCatching {
                        block()
                    }.onFailure {
                        it.printStackTrace()
                    }
                    kotlinx.coroutines.delay(1000 * 60 * 60 * 24)
                }
            } else {
                kotlinx.coroutines.delay(firstDelay)
                kotlin.runCatching {
                    block()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun atTime(atTime: String, always: Boolean = false, runnable: Runnable): Job {
        val firstDelay = firstDelay(atTime)
        return coroutineScope.launch {
            if (always) {
                kotlinx.coroutines.delay(firstDelay)
                while (true) {
                    kotlin.runCatching {
                        runnable.run()
                    }.onFailure {
                        it.printStackTrace()
                    }
                    kotlinx.coroutines.delay(1000 * 60 * 60 * 24)
                }
            } else {
                kotlinx.coroutines.delay(firstDelay)
                kotlin.runCatching {
                    runnable.run()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

}