package com.sty.kotlincoroutine

import com.google.gson.Gson
import com.sty.api.User
import com.sty.api.userServiceApi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.junit.Test
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

private val cachePath = "E://test//coroutine.cache"
private val gson = Gson()

data class Response<T>(val value: T, val isLocal: Boolean)

fun CoroutineScope.getUserFromLocal(lastName: String) = async(Dispatchers.IO){
    //delay(1000) //故意的延迟
    File(cachePath).readText().let{
        gson.fromJson(it, User::class.java)
    }
}

fun CoroutineScope.getUserFromRemote(lastName: String) = async(Dispatchers.IO) {
    userServiceApi.getUser()
}

/**
 * Author: ShiTianyi
 * Time: 2022/2/11 0011 18:10
 * Description: 协程多路复用
 */

class CoroutineMultiSelectTest {
    @Test
    fun testSelectAwait() = runBlocking<Unit> {
        GlobalScope.launch {
            val localRequest = getUserFromLocal("xxx")
            val remoteRequest = getUserFromRemote("xxx")
            val userResponse = select<Response<User>> {
                localRequest.onAwait{ Response(it, true)}
                remoteRequest.onAwait{ Response(it, false)}
            }
            userResponse.value?.let{ println(it) }
        }.join()
    }
    //User(lastName=Jack lall, age=23)

    @Test
    fun testSelectChannel() = runBlocking<Unit> {
        val channels = listOf(Channel<Int>(), Channel<Int>())
        GlobalScope.launch {
            delay(100)
            channels[0].send(200)
        }

        GlobalScope.launch {
            delay(50)
            channels[1].send(100)
        }

        val result = select<Int?> {
            channels.forEach { channel ->
                channel.onReceive { it }
            }
        }
        println(result)
    }
    //100

    @Test
    fun testSelectClause0() = runBlocking<Unit> {
        val job1 = GlobalScope.launch {
            delay(100)
            println("job 1")
        }
        val job2 = GlobalScope.launch {
            delay(10)
            println("job 2")
        }

        select<Unit> {
            job1.onJoin { println("job 1 onJoin") }
            job2.onJoin { println("job 2 onJoin") }
        }
        delay(1000)
    }
    //job 2
    //job 2 onJoin
    //job 1

    @Test
    fun testSelectClause2() = runBlocking<Unit> {
        val channels = listOf(Channel<Int>(), Channel<Int>())
        println(channels)

        launch(Dispatchers.IO) {
            select<Unit?> {
                launch {
                    delay(10)
                    channels[1].onSend(200) { sentChannel ->
                        println("sent on $sentChannel")
                    }
                }
                launch {
                    delay(100)
                    channels[0].onSend(100) { sentChannel ->
                        println("sent on $sentChannel")
                    }
                }
            }
        }

        GlobalScope.launch {
            println(channels[0].receive())
        }
        GlobalScope.launch {
            println(channels[1].receive())
        }
        delay(1000)
    }
    //[RendezvousChannel@61824c18{EmptyQueue}, RendezvousChannel@7ac5bc56{EmptyQueue}]
    //200

    @Test
    fun testSelectFlow() = runBlocking<Unit> {
        //函数 -> 协程 -> Flow -> Flow合并
        val name = "guess"
        coroutineScope {
            listOf(::getUserFromLocal, ::getUserFromRemote)
                .map { function ->
                    function.call(name)
                }.map { deferred ->
                    flow { emit(deferred.await()) }
                }.merge()
                .collect { user -> println(user) }
        }
    }
    //User(lastName=Jack lall, age=23)
    //User(lastName=张三, age=18)

    @Test
    fun testNotSafeConcurrent() = runBlocking<Unit> {
        var count = 0
        List(1000) {
            GlobalScope.launch { count++ }
        }.joinAll()
        println(count)
    }
    //994

    @Test
    fun testSafeConcurrent() = runBlocking<Unit> {
        var count = AtomicInteger(0)
        List(1000) {
            GlobalScope.launch { count.incrementAndGet() }
        }.joinAll()
        println(count)
    }
    //1000

    @Test
    fun testSafeConcurrentMutex() = runBlocking<Unit> {
        var count = 0
        val mutex = Mutex()
        List(1000) {
            GlobalScope.launch {
                mutex.withLock {
                    count++
                }
            }
        }.joinAll()
        println(count)
    }
    //1000

    @Test
    fun testSafeConcurrentSemaphore() = runBlocking<Unit> {
        var count = 0
        val semaphore = Semaphore(1)
        List(1000) {
            GlobalScope.launch {
                semaphore.withPermit {
                    count++
                }
            }
        }.joinAll()
        println(count)
    }
    //1000

    @Test
    fun testAvoidAccessOuterVariable() = runBlocking<Unit> {
        var count = 0
        val result = count + List(1000){
            GlobalScope.async { 1 }
        }.map { it.await() }.sum()
        println(result)
    }
    //1000
}