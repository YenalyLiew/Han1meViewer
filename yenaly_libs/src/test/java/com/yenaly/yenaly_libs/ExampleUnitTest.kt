package com.yenaly.yenaly_libs

import com.yenaly.yenaly_libs.utils.formatPlayCount
import com.yenaly.yenaly_libs.utils.fromJson
import com.yenaly.yenaly_libs.utils.secondToTimeCase
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun json_isCorrect() {
        val foobar = """
            [{"foo":"123","bar":"456"},{"foo":"789","bar":"012"}]
        """.trimIndent()
        val bar = foobar.fromJson<List<TestBean>>()
        println(bar[1].foo)
    }

    data class TestBean(val foo: String)

    @Test
    fun playCount_isCorrect() {
        val playCount1 = 785L
        val playCount2 = 5678L
        val playCount3 = 46463L
        val format3 = playCount3.formatPlayCount()
        println(format3)
    }

    @Test
    fun timeCase_isCorrect() {
        val case1 = 785L
        val case2 = 5678L
        val case3 = 46463L
        val case4 = 12L
        val case5 = 144234L
        println(case1.secondToTimeCase())
        println(case2.secondToTimeCase())
        println(case3.secondToTimeCase())
        println(case4.secondToTimeCase())
        println(case5.secondToTimeCase())
    }
}