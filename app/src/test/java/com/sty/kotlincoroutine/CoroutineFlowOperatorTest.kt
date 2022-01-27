package com.sty.kotlincoroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Author: ShiTianyi
 * Time: 2022/1/27 0027 17:24
 * Description:
 */
class CoroutineFlowOperatorTest {
    @Test
    fun testTransformFlowOperator1() = runBlocking<Unit> {
        (1..3).asFlow()
            .map {
                delay(1000)
                "response $it"
            }.collect {
                println(it)
            }
    }
    //response 1
    //response 2
    //response 3

    suspend fun performRequest(request: Int): String {
        delay(1000)
        return "response $request"
    }

    @Test
    fun testTransformFlowOperator2() = runBlocking<Unit> {
        (1..3).asFlow()
            .transform {
                emit("Making request $it")
                emit(performRequest(it))
            }.collect {
                println(it)
            }
    }
    //Making request 1
    //response 1
    //Making request 2
    //response 2
    //Making request 3
    //response 3

    @Test
    fun testLimitFlowOperator() = runBlocking<Unit> {
        flow<Int> {
            try {
                emit(1)
                emit(2)
                println("This line will not execute")
                emit(3)
            } finally {
                println("Finally in numbers")
            }
        }.take(2)
            .collect { println(it)}
    }
    //1
    //2
    //Finally in numbers

    @Test
    fun testTerminalOperator() = runBlocking<Unit> {
        val sum = (1..5).asFlow()
            .map { it * it }
            .reduce { accumulator, value ->
                println("$accumulator --> $value")
                accumulator + value
            }
        println(sum)
    }
    //1 --> 4
    //5 --> 9
    //14 --> 16
    //30 --> 25
    //55
}