package com.sty.kotlincoroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Author: ShiTianyi
 * Time: 2022/2/10 0010 20:26
 * Description:
 */
class CoroutineFlowExceptionTest {

    fun simpleFlow() = flow<Int> {
        for(i in 1..3) {
            println("Emitting $i")
            emit(i)
        }
    }

    //下游捕获异常
    @Test
    fun testFlowException() = runBlocking<Unit> {
        try {
            simpleFlow().collect {
                println(it)
                check(it <= 1) { println("Collected $it") }
            }
        }catch (e: Throwable) {
            println("Caught $e")
        }
    }
    //Emitting 1
    //1
    //Emitting 2
    //2
    //Collected 2
    //Caught java.lang.IllegalStateException: kotlin.Unit

    //上游捕获异常
    @Test
    fun testFlowException2() = runBlocking<Unit> {
        flow {
            throw ArithmeticException("Div 0")
            emit(1)
        }.catch { e: Throwable ->
            println("caught $e")
            emit(10) //在异常中恢复
        }.flowOn(Dispatchers.IO)
            .collect{ println(it) }
    }
    //caught java.lang.ArithmeticException: Div 0
    //10

    @Test
    fun testFlowCompleteInFinally() = runBlocking<Unit> {
        try {
            (1..3).asFlow().collect{ println(it) }
        } finally {
            println("Done")
        }
    }
    //1
    //2
    //3
    //Done

    @Test
    fun testFlowCompleteInCompletion1() = runBlocking<Unit> {
        flow {
            throw ArithmeticException("Div 0")
            emit(1)
        }.onCompletion { e -> //可以拿到上游的异常（但不会阻止崩溃 --> 需要用catch）
            if(e != null) println("Flow completed with exception: $e")
        }.collect { println(it) }
        //Flow completed with exception: java.lang.ArithmeticException: Div 0
        //Div 0
        //java.lang.ArithmeticException: Div 0
    }

    @Test
    fun testFlowCompleteInCompletion2() = runBlocking<Unit> {
        (1..3).asFlow()
            .onCompletion { e -> //可以拿到下游的异常（但不会阻止崩溃 --> 需要用catch）
                if(e != null) println("Flow completed with exception: $e")
            }.collect {
                println(it)
                check(it <= 1) { println("Collected $it") }
            }
        //1
        //2
        //Collected 2
        //Flow completed with exception: java.lang.IllegalStateException: kotlin.Unit
        //kotlin.Unit
        //java.lang.IllegalStateException: kotlin.Unit
    }


}