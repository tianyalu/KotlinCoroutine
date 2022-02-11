package com.sty.kotlincoroutine

import kotlinx.coroutines.*
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.system.measureTimeMillis

/**
 * Author: ShiTianyi
 * Time: 2022/1/14 0014 19:41
 * Description: 协程启动
 */
class CoroutineLauncherTest {

    @Test
    fun testCoroutineBuilder() = runBlocking {
        val job1 = launch {
            delay(200)
            println("job1 finished")
        }

        val job2 = async {
            delay(200)
            println("job2 finished")
            "job2 result"
        }

        println(job2.await())
    }
    //job1 finished
    //job2 finished
    //job2 result


    @Test
    fun testCoroutineJoin() = runBlocking {
        val job1 = launch {
            delay(2000)
            println("One")
        }
        job1.join() //等待job1启动完毕之后再启动job2和job3,否则会先打印后面两个

        val job2 = launch {
            delay(200)
            println("Two")
        }

        val job3 = launch {
            delay(200)
            println("Three")
        }
    }
    //One
    //Two
    //Three


    @Test
    fun testCoroutineAwait() = runBlocking {
        val job1 = async {
            delay(2000)
            println("One")
        }
        job1.join() //等待job1启动完毕之后再启动job2和job3,否则会先打印后面两个

        val job2 = async {
            delay(200)
            println("Two")
        }

        val job3 = async {
            delay(200)
            println("Three")
        }
    }
    //One
    //Two
    //Three


    @Test
    fun testSync() = runBlocking {
        val time = measureTimeMillis {
            val one = doOne()
            val two = doTwo()
            println("The result:${one + two}")
        }
        println("Completed in $time ms")
    }
    //The result:39
    //Completed in 3036 ms

    @Test
    fun testCombineSync() = runBlocking {
        val time = measureTimeMillis {
            val one = async { doOne() }
            val two = async { doTwo() }
            println("The result:${one.await() + two.await()}")
        }
        println("Completed in $time ms")
    }
    //The result:39
    //Completed in 2036 ms

    private suspend fun doOne(): Int {
        delay(1000)
        return 14
    }

    private suspend fun doTwo(): Int {
        delay(2000)
        return 25
    }


    @Test
    fun testStartMode() = runBlocking {
        val job = launch {
            delay(10000)
            println("Job finished.")
        }
        delay(1000)
        job.cancel()

        val job2 = async(context = Dispatchers.IO, start = CoroutineStart.DEFAULT) {
            println("thread1: ${Thread.currentThread().name}")
            delay(1000)
            print("thread2: ${Thread.currentThread().name}")
        }
        job2.await()
    }
    //thread1: Test worker @coroutine#3
    //CoroutineStart.DEFAULT
    //thread1: DefaultDispatcher-worker-1 @coroutine#3


    @Test
    fun testCoroutineScopeBuilder() = runBlocking {
        coroutineScope {  //继承父协程的协程作用域
            val job1 = launch {
                delay(200)
                println("job1 finished")
                throw IllegalArgumentException()
            }

            val job2 = async {
                delay(400)
                println("job2 finished")
                "job2 result"
            }
        }
    }
    //job1 finished
    //java.lang.IllegalArgumentException

    @Test
    fun testSupervisorScopeBuilder() = runBlocking {
        supervisorScope {
            val job1 = launch {
                delay(200)
                println("job1 finished")
                throw IllegalArgumentException()
            }

            val job2 = async {
                delay(400)
                println("job2 finished")
                "job2 result"
            }
        }
    }
    //job1 finished
    //Exception in thread "Test worker @coroutine#2" java.lang.IllegalArgumentException
    //job2 finished
}