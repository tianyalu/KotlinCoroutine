package com.sty.kotlincoroutine

import kotlinx.coroutines.*
import org.junit.Test
import java.io.BufferedReader
import java.io.FileReader

/**
 * Author: ShiTianyi
 * Time: 2022/1/17 0017 19:39
 * Description:
 */
class CoroutineCancelTest {

    @Test
    fun testCoroutineCancel() = runBlocking<Unit> {
        val scope = CoroutineScope(Dispatchers.Default) //构建自己的协程作用域，未继承父协程的协程作用域
        scope.launch {
            delay(1000)
            println("job 1")
        }
        scope.launch {
            delay(1000)
            println("job 2")
        }

        delay(100)
        scope.cancel()

        delay(10000) //这里的scope不属于主线程，不会阻塞，所以这里需要阻塞才能查看效果
    }

    @Test
    fun testCoroutineBrotherCancel() = runBlocking<Unit> {
        val scope = CoroutineScope(Dispatchers.Default) //构建自己的协程作用域，未继承父协程的协程作用域
        val job1 = scope.launch {
            delay(1000)
            println("job 1")
        }
        val job2 = scope.launch {
            delay(1000)
            println("job 2")
        }

        delay(100)
        job1.cancel()

        delay(10000) //这里的scope不属于主线程，不会阻塞，所以这里需要阻塞才能查看效果
    }
    //job 2

    @Test
    fun testCancellationException() = runBlocking<Unit> {
        val job1 = GlobalScope.launch {
            try {
                delay(1000)
                println("job 1")
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

        delay(100)
        //job1.cancel(CancellationException("取消"))
        //java.util.concurrent.CancellationException: 取消
        //job1.cancel()
        //kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled
        //job1.join() //阻塞父协程

        job1.cancelAndJoin()
    }

    @Test
    fun testCancelActiveCPUTask() = runBlocking<Unit> {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            //while (i < 5 && isActive) { //采用这种方式就可以退出了
            while (i < 5) {
                if(System.currentTimeMillis() >= nextPrintTime) {
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500
                }
            }
        }

        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }
//    job: I'm sleeping 0 ...
//    job: I'm sleeping 1 ...
//    job: I'm sleeping 2 ...
//    main: I'm tired of waiting!
//    job: I'm sleeping 3 ...
//    job: I'm sleeping 4 ...
//    main: Now I can quit.


    @Test
    fun testCancelEnsureActiveCPUTask() = runBlocking<Unit> {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            //while (i < 5 && isActive) { //采用这种方式就可以退出了
            while (i < 5) {
                try {
                    ensureActive()  //采用这种方式也可以退出，会抛出异常（可以用try catch捕获）
                    if(System.currentTimeMillis() >= nextPrintTime) {
                        println("job: I'm sleeping ${i++} ...")
                        nextPrintTime += 500
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                    break
                }

            }
        }

        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }
//    job: I'm sleeping 0 ...
//    job: I'm sleeping 1 ...
//    job: I'm sleeping 2 ...
//    main: I'm tired of waiting!
//    kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled;
//    main: Now I can quit.

    @Test
    fun testCancelYieldActiveCPUTask() = runBlocking<Unit> {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            //while (i < 5 && isActive) { //采用这种方式就可以退出了
            while (i < 5) {
                try {
                    yield()  //采用这种方式也可以退出，可以尝试出让CPU执行权，会抛出异常（可以用try catch捕获）
                    if(System.currentTimeMillis() >= nextPrintTime) {
                        println("job: I'm sleeping ${i++} ...")
                        nextPrintTime += 500
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                    break
                }

            }
        }

        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }
//    job: I'm sleeping 0 ...
//    job: I'm sleeping 1 ...
//    job: I'm sleeping 2 ...
//    main: I'm tired of waiting!
//    kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled;
//    main: Now I can quit.

    @Test
    fun testReleaseResources() = runBlocking<Unit> {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job I'm sleeping $i ...")
                    delay(500L)
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }finally {
                println("job: I'm running finally")
            }
        }

        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }
//    job I'm sleeping 0 ...
//    job I'm sleeping 1 ...
//    job I'm sleeping 2 ...
//    main: I'm tired of waiting!
//    kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled;
//    job: I'm running finally
//    main: Now I can quit.

    @Test
    fun testUseFunction() = runBlocking<Unit> {
//        var br = BufferedReader(FileReader("E:\\test\\data.txt"))
//        with(br) {
//            var line: String?
//            try {
//                while (true) {
//                    line = readLine() ?: break
//                    println(line)
//                }
//            }catch (e: Exception) {
//                e.printStackTrace()
//            }finally {
//                close()
//            }
//        }

        BufferedReader(FileReader("E:\\test\\data.txt")).use {
            var line: String?
            while (true) {
                line = it.readLine() ?: break
                println(line)
            }
        }
    }

    @Test
    fun testCancelWithNonCancellable() = runBlocking<Unit> {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job I'm sleeping $i ...")
                    delay(500L)
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }finally {
                println("job: I'm running finally")
                withContext(NonCancellable){ //如果不放在NonCancellable CoroutineContext 中以下代码不会被执行
                    delay(1000)
                    println("job: And I've just delayed for 1 sec because I'm non-cancellable")
                }
            }
        }

        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }
//    job I'm sleeping 0 ...
//    job I'm sleeping 1 ...
//    job I'm sleeping 2 ...
//    main: I'm tired of waiting!
//    kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled;
//    job: I'm running finally
//    job: And I've just delayed for 1 sec because I'm non-cancellable
//    main: Now I can quit.

    @Test
    fun testDealWithTimeout() = runBlocking<Unit> {
//        withTimeout(1300) {
//            repeat(1000) { i ->
//                println("job: I'm sleeping $i ...")
//                delay(500)
//            }
//        }
        //    job: I'm sleeping 0 ...
        //    job: I'm sleeping 1 ...
        //    job: I'm sleeping 2 ...
        //    Timed out waiting for 1300 ms
        //    kotlinx.coroutines.TimeoutCancellationException: Timed out waiting for 1300 ms

        val result = withTimeoutOrNull(1300) {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500)
            }
            "Done"
        }
        println("Result is $result")
        //        job: I'm sleeping 0 ...
        //        job: I'm sleeping 1 ...
        //        job: I'm sleeping 2 ...
        //        Result is null
    }


}