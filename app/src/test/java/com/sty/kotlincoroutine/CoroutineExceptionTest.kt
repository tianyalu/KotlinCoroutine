package com.sty.kotlincoroutine

import kotlinx.coroutines.*
import org.junit.Test
import java.io.IOException
import kotlin.ArithmeticException
import kotlin.IllegalArgumentException
import kotlin.IndexOutOfBoundsException
import kotlin.coroutines.CoroutineContext

/**
 * Author: ShiTianyi
 * Time: 2022/1/18 0018 19:27
 * Description:
 */
class CoroutineExceptionTest {

    @Test
    fun testCoroutineContext() = runBlocking<Unit> {
        launch(Dispatchers.Default + CoroutineName("test")) {
            println("I'm working in thread ${Thread.currentThread().name}")
        }
    }
    //I'm working in thread DefaultDispatcher-worker-2 @test#2

    @Test
    fun testCoroutineContextExtend() = runBlocking<Unit> {
        val scope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("test"))
        val job = scope.launch {
            println("1. ${coroutineContext[Job]}  ${Thread.currentThread().name}")
            val result = async {
                println("2. ${coroutineContext[Job]}  ${Thread.currentThread().name}")
                "OK"
            }.await()
        }
        job.join()
    }
    //1. "test#2":StandaloneCoroutine{Active}@11ca8e94  DefaultDispatcher-worker-2 @test#2
    //2. "test#3":DeferredCoroutine{Active}@7b529531  DefaultDispatcher-worker-1 @test#3

    @Test
    fun testCoroutineContextExtend2() = runBlocking<Unit> {
        val coroutineExceptionHandler = CoroutineExceptionHandler{ _, exception ->
            println("Caught $exception")
        }
        val scope = CoroutineScope(Job() + Dispatchers.Main + coroutineExceptionHandler)
        val job = scope.launch(Dispatchers.IO) {
            //新协程
            println("${coroutineContext[Job]}  ${Thread.currentThread().name}")
        }
        job.join()
    }
    //"coroutine#2":StandaloneCoroutine{Active}@5462fed8  DefaultDispatcher-worker-1 @coroutine#2

    @Test
    fun testExceptionPropagation() = runBlocking<Unit> {
        val job = GlobalScope.launch {
            try {
                throw IndexOutOfBoundsException()
            }catch (e: Exception) {
                println("Caught IndexOutOfBoundsException")
            }
        }
        job.join()

        val deferred = GlobalScope.async {
            throw ArithmeticException()
        }
//        try {
//            deferred.await()
//        }catch (e: Exception) {
//            println("Caught ArithmeticException")
//        }

        delay(10000)
    }
    //Caught IndexOutOfBoundsException
    //Caught ArithmeticException

    @Test
    fun testExceptionPropagation2() = runBlocking<Unit> {
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            async {
                throw IllegalArgumentException()
                //如果async抛出异常，launch 会立即抛出异常，而不会调用 .await()
            }
        }
        job.join()
    }
    //Exception in thread "DefaultDispatcher-worker-2 @coroutine#3" java.lang.IllegalArgumentException

    @Test
    fun testSupervisorJob() = runBlocking<Unit> {
        val supervisor = CoroutineScope(SupervisorJob())
        //child 1
        //Exception in thread "DefaultDispatcher-worker-1 @coroutine#2" java.lang.IllegalArgumentException

        //val supervisor = CoroutineScope(Job())
        //child 1
        //Exception in thread "DefaultDispatcher-worker-1 @coroutine#2" java.lang.IllegalArgumentException
        //child 2 finished.
        val job1 = supervisor.launch {
            delay(100)
            println("child 1")
            throw IllegalArgumentException()
        }

        val job2 = supervisor.launch {
            try {
                delay(Long.MAX_VALUE)
            }finally {
                println("child 2 finished.")
            }
        }
        delay(200)
        supervisor.cancel()
        joinAll(job1, job2)
    }

    @Test
    fun testSupervisorScope() = runBlocking<Unit> {
        supervisorScope {
            launch {
                delay(100)
                println("child 1")
                throw IllegalArgumentException()
            }

            try {
                delay(Long.MAX_VALUE)
            }finally {
                println("child 2 finished.")
            }
        }
    }

    @Test
    fun testSupervisorScope2() = runBlocking<Unit> {
        try {
            supervisorScope {
                val child = launch {
                    try {
                        println("The child is sleeping")
                        delay(Long.MAX_VALUE)
                    }finally {
                        println("The child is cancelled")
                    }
                }
                yield() //使用yield来给我们的子作业一个机会来执行打印
                println("Throwing an exception from the scope")
                throw AssertionError()
            }
        }catch (e: AssertionError) {
            println("Caught an assertion error")
        }
    }
    //The child is sleeping
    //Throwing an exception from the scope
    //The child is cancelled
    //Caught an assertion error

    @Test
    fun testCoroutineExceptionHandler() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler{ _, exception ->
            println("Caught $exception")
        }

        val job = GlobalScope.launch(handler) {
            throw AssertionError()
        }

        val deferred = GlobalScope.async(handler) {
            throw ArithmeticException()
        }

        job.join()
        deferred.await()
    }
    //Caught java.lang.AssertionError
    //java.lang.ArithmeticException at com.sty.kotlincoroutine....

    @Test
    fun testCoroutineExceptionHandler2() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler{ _, exception ->
            println("Caught $exception")
        }
        val scope = CoroutineScope(Job())
        val job = scope.launch(handler) { //放在这里可以被handler捕获到，因为handler直接在scope这一级
            launch{
                throw IllegalArgumentException()
            }
        }
        job.join()
    }
    //Caught java.lang.IllegalArgumentException

    @Test
    fun testCoroutineExceptionHandler3() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler{ _, exception ->
            println("Caught $exception")
        }
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            launch(handler){  //放在这里不能被handler捕获到，因为handler在子协程中
                throw IllegalArgumentException()
            }
        }
        job.join()
    }
    //Exception in thread "DefaultDispatcher-worker-2 @coroutine#3" java.lang.IllegalArgumentException

    @Test
    fun testCancelAndException() = runBlocking<Unit> {
        val job = launch {
            val child = launch {
                try {
                    delay(Long.MAX_VALUE)
                }finally {
                    println("Child is cancelled")
                }
            }
            yield()
            println("Cancelling child")
            child.cancelAndJoin()
            //yield()
            println("Parent is not cancelled")
        }
        job.join()
    }
    //Cancelling child
    //Child is cancelled
    //Parent is not cancelled

    @Test
    fun testCancelAndException2() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler{ _, exception ->
            println("Caught $exception")
        }

        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                }finally {
                    withContext(NonCancellable) {
                        println("Children are cancelled, but exception is not handled until all children terminate")
                        delay(100)
                        println("The first child finished its non cancellable block")
                    }
                }
            }

            launch {
                delay(10)
                println("Second child throws an exception")
                throw ArithmeticException()
            }
        }
        job.join()
    }
    //Second child throws an exception
    //Children are cancelled, but exception is not handled until all children terminate
    //The first child finished its non cancellable block
    //Caught java.lang.ArithmeticException

    @Test
    fun testExceptionAggregation() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler{ _, exception ->
            println("Caught 1.$exception 2.${exception.suppressed.contentToString()}")
        }

        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                }finally {
                    throw ArithmeticException() //2
                }
            }

            launch {
                try {
                    delay(Long.MAX_VALUE)
                }finally {
                    throw IndexOutOfBoundsException() //3
                }
            }

            launch {
                delay(100)
                throw IOException() //1
            }
        }
        job.join()
    }
    //Caught 1.java.io.IOException 2.[java.lang.IndexOutOfBoundsException, java.lang.ArithmeticException]
}