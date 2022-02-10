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

    @Test
    fun testZipOperator() = runBlocking<Unit> {
        val numbers = (1..3).asFlow().onEach { delay(300) }
        val strs = flowOf("One", "Two", "Three").onEach { delay(400) }
        val startTime = System.currentTimeMillis()
        numbers.zip(strs) {
            a, b -> "$a -> $b"
        }.collect{
            println("$it at ${System.currentTimeMillis() - startTime} ms from start")
        }
    }
    //1 -> One at 433 ms from start
    //2 -> Two at 840 ms from start
    //3 -> Three at 1247 ms from start

    fun requestFlow(i: Int) = flow<String> {
        emit("$i: First")
        delay(500)
        emit("$i: Second")
    }

    @Test
    fun testFlatMapConcatOperator() = runBlocking<Unit> {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach { delay(100) }
            //.map{ requestFlow(it)} // --> Flow
            .flatMapConcat { requestFlow(it) }
            .collect {
                println("$it at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
    //1: First at 156 ms from start
    //1: Second at 664 ms from start
    //2: First at 773 ms from start
    //2: Second at 1274 ms from start
    //3: First at 1382 ms from start
    //3: Second at 1898 ms from start

    @Test
    fun testFlatMapMergeOperator() = runBlocking<Unit> {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach { delay(100) }
            .flatMapMerge { requestFlow(it) }
            .collect {
                println("$it at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
    //1: First at 178 ms from start
    //2: First at 280 ms from start
    //3: First at 392 ms from start
    //1: Second at 686 ms from start
    //2: Second at 795 ms from start
    //3: Second at 907 ms from start

    @Test
    fun testFlatMaLatestOperator() = runBlocking<Unit> {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach { delay(100) }
            .flatMapLatest { requestFlow(it) }
            .collect {
                println("$it at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
    //1: First at 167 ms from start
    //2: First at 301 ms from start
    //3: First at 412 ms from start
    //3: Second at 930 ms from start
}