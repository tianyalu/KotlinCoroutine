package com.sty.kotlincoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Author: ShiTianyi
 * Time: 2022/1/25 0025 20:13
 * Description: 流
 */
class CoroutineFlowTest {

    //返回了多个值，但不是异步的
    fun simpleList(): List<Int> = listOf<Int>(1, 2, 3)

    //返回了多个值，同步的
    fun simpleSequence(): Sequence<Int> = sequence {
        for (i in 1..3) {
            Thread.sleep(1000) //阻塞，假装在计算
            //delay(1000) //报错：SequenceScope中只能调用它自己已经定义的挂起函数（RestrictsSuspension注解）
            yield(i)
        }
    }

    //返回了多个值，异步，但是是一次性返回的
    suspend fun simpleList2(): List<Int> {
        delay(1000)
        return listOf(1, 2, 3)
    }

    //返回多个值，并且是异步的
    fun simpleFlow() = flow<Int> {
        for (i in 1..3) {
            delay(1000) //假装在做重要的事情
            emit(i) //发射，产生一个元素
        }
    }

    @Test
    fun testMultipleValues() {
        //simpleList().forEach { value -> println(value) }
        simpleSequence().forEach { value -> println(value) }
    }

    @Test
    fun testMultipleValues2() = runBlocking<Unit> {
        simpleList2().forEach { value -> println(value) }
    }

    @Test
    fun testMultipleValues3() = runBlocking<Unit> {

        launch {
            for (k in 1..3) {
                println("I'm not blocked $k")
                delay(1500)
            }
        }
        simpleFlow().collect { value -> println(value) }
    }


    fun simpleFlow2() = flow<Int> {
        println("Flow started")
        for (i in 1..3) {
            delay(1000)
            emit(i) //发射，产生一个元素
        }
    }

    @Test
    fun testFlowIsCold() = runBlocking<Unit> {
        val flow = simpleFlow2()
        println("Calling collect...")
        flow.collect { value -> println(value) }
        println("Calling collect again...")
        flow.collect { value -> println(value) }
    }
    //Calling collect...
    //Flow started
    //1
    //2
    //3
    //Calling collect again...
    //Flow started
    //1
    //2
    //3

    @Test
    fun testFlowContinuation() = runBlocking<Unit> {
        (1..5).asFlow().filter {
            it % 2 == 0
        }.map {
            "string $it"
        }.collect {
            println("Collect $it")
        }
    }
    //Collect string 2
    //Collect string 4

    @Test
    fun testFlowBuilder() = runBlocking<Unit> {
//        flowOf("one", "two", "three")
//            .onEach { delay(1000) }
//            .collect { println(it) }

        (1..3).asFlow()
            .collect { println(it) }
    }

    fun simpleFlow3() = flow<Int> {
        println("Flow started ${Thread.currentThread().name}")
        for (i in 1..3) {
            delay(1000)
            emit(i) //发射，产生一个元素
        }
    }

    @Test
    fun testFlowContext() = runBlocking<Unit> {
        simpleFlow3().collect { println("Collected $it ${Thread.currentThread().name}") }
    }
    //Flow started Test worker @coroutine#1
    //Collected 1 Test worker @coroutine#1
    //Collected 2 Test worker @coroutine#1
    //Collected 3 Test worker @coroutine#1

    fun simpleFlow4() = flow<Int> {
        println("Flow started ${Thread.currentThread().name}")
        for (i in 1..3) {
            delay(1000)
            emit(i) //发射，产生一个元素
        }
    }.flowOn(Dispatchers.Default)

    @Test
    fun testFlowOn() = runBlocking<Unit> {
        simpleFlow4().collect { println("Collected $it ${Thread.currentThread().name}") }
    }
    //Flow started DefaultDispatcher-worker-1 @coroutine#2
    //Collected 1 Test worker @coroutine#1
    //Collected 2 Test worker @coroutine#1
    //Collected 3 Test worker @coroutine#1

    fun events() = (1..3)
        .asFlow()
        .onEach { delay(100) }
        .flowOn(Dispatchers.Default)

    @Test
    fun testFlowLaunch() = runBlocking<Unit> {
        events().onEach { println("Event: $it ${Thread.currentThread().name}") }
            .launchIn(CoroutineScope(Dispatchers.IO))
            .join()
        //.launchIn(this)
    }
    //Event: 1 DefaultDispatcher-worker-3 @coroutine#2
    //Event: 2 DefaultDispatcher-worker-1 @coroutine#2
    //Event: 3 DefaultDispatcher-worker-1 @coroutine#2

    fun simpleFlow5() = flow<Int> {
        for (i in 1..3) {
            delay(1000)
            emit(i) //发射，产生一个元素
            println("Emitting $i")
        }
    }

    @Test
    fun testFlowCancel() = runBlocking<Unit> {
        withTimeoutOrNull(2500) {
            simpleFlow5().collect { println(it) }
        }
        println("Done")
    }
    //1
    //Emitting 1
    //2
    //Emitting 2
    //Done

    @Test
    fun testFlowCancelCheck1() = runBlocking<Unit> {
        flow<Int> {
            for (i in 1..5) {
                emit(i)
                println("Emitting $i")
            }
        }.collect {
            println(it)
            if (it == 3) {
                cancel()
            }
        }
    }
    //1
    //Emitting 1
    //2
    //Emitting 2
    //3
    //Emitting 3
    //BlockingCoroutine was cancelled
    //kotlinx.coroutines.JobCancellationException: BlockingCoroutine was cancelled;

    @Test
    fun testFlowCancelCheck2() = runBlocking<Unit> {
        (1..5).asFlow()
            .cancellable() //不加这个虽然也会报取消异常，但是依然会打印4、5，因为繁忙循环不会自行执行其它取消检测
            .collect {
                println(it)
                if (it == 3) {
                    cancel()
                }
            }
    }
    //1
    //2
    //3
    //BlockingCoroutine was cancelled
    //kotlinx.coroutines.JobCancellationException: BlockingCoroutine was cancelled;

    fun simpleFlow6() = flow {
        for (i in 1..3) {
            delay(100)
            println("Emitting $i ${Thread.currentThread().name}")
            emit(i)
        }
    }

    @Test
    fun testFlowBackPressure() = runBlocking<Unit> {
        val time = measureTimeMillis {
            simpleFlow6()
                .collect {
                    delay(300)
                    println("Collected $it ${Thread.currentThread().name}")
                }
        }
        println("collected in $time ms")
    }
    //Emitting 1 Test worker @coroutine#1
    //Collected 1 Test worker @coroutine#1
    //Emitting 2 Test worker @coroutine#1
    //Collected 2 Test worker @coroutine#1
    //Emitting 3 Test worker @coroutine#1
    //Collected 3 Test worker @coroutine#1
    //collected in 1296 ms

    @Test
    fun testFlowBackPressureBuffer() = runBlocking<Unit> {
        val time = measureTimeMillis {
            simpleFlow6().buffer(50) //不切换线程，并发运行流中发射的元素
                .collect {
                    delay(300)
                    println("Collected $it ${Thread.currentThread().name}")
                }
        }
        println("collected in $time ms")
    }
    //Emitting 1 Test worker @coroutine#2
    //Emitting 2 Test worker @coroutine#2
    //Emitting 3 Test worker @coroutine#2
    //Collected 1 Test worker @coroutine#1
    //Collected 2 Test worker @coroutine#1
    //Collected 3 Test worker @coroutine#1
    //collected in 1091 ms

    @Test
    fun testFlowBackPressureFlowOn() = runBlocking<Unit> {
        val time = measureTimeMillis {
            simpleFlow6().flowOn(Dispatchers.Default)  //切换了线程，同步发射
                .collect {
                    delay(300)
                    println("Collected $it ${Thread.currentThread().name}")
                }
        }
        println("collected in $time ms")
    }
    //Emitting 1 DefaultDispatcher-worker-1 @coroutine#2
    //Emitting 2 DefaultDispatcher-worker-1 @coroutine#2
    //Emitting 3 DefaultDispatcher-worker-1 @coroutine#2
    //Collected 1 Test worker @coroutine#1
    //Collected 2 Test worker @coroutine#1
    //Collected 3 Test worker @coroutine#1
    //collected in 1101 ms

    @Test
    fun testFlowBackPressureConflate() = runBlocking<Unit> {
        val time = measureTimeMillis {
            simpleFlow6().conflate()  //不切换线程，合并发射项，不对每个值进行处理
                .collect {
                    delay(300)
                    println("Collected $it ${Thread.currentThread().name}")
                }
        }
        println("collected in $time ms")
    }
    //Emitting 1 Test worker @coroutine#2
    //Emitting 2 Test worker @coroutine#2
    //Emitting 3 Test worker @coroutine#2
    //Collected 1 Test worker @coroutine#1
    //Collected 3 Test worker @coroutine#1
    //collected in 799 ms

    @Test
    fun testFlowBackPressureCollectLatest() = runBlocking<Unit> {
        val time = measureTimeMillis {
            simpleFlow6()
                .collectLatest { //取消并重新发射最后一个值
                    delay(300)
                    println("Collected $it ${Thread.currentThread().name}")
                }
        }
        println("collected in $time ms")
    }
    //Emitting 1 Test worker @coroutine#2
    //Emitting 2 Test worker @coroutine#2
    //Emitting 3 Test worker @coroutine#2
    //Collected 3 Test worker @coroutine#5
    //collected in 746 ms


}