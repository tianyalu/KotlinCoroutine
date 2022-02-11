package com.sty.kotlincoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.junit.Test

/**
 * Author: ShiTianyi
 * Time: 2022/2/11 0011 14:36
 * Description:
 */
class CoroutineChannelTest {
    @Test
    fun testChannelKnown() = runBlocking<Unit> {
        val channel = Channel<Int>()
        //生产者
        val producer = GlobalScope.launch {
            var i = 0
            while (true) {
                delay(1000)
                channel.send(++i)
                println("send $i")
            }
        }

        //消费者
        val consumer = GlobalScope.launch {
            while (true) {
                delay(2000)
                val element = channel.receive()
                println("receive $element")
            }
        }
        joinAll(producer, consumer)
    }
    //send 1
    //receive 1
    //send 2
    //receive 2
    //send 3
    //receive 3
    //...

    @Test
    fun testChannelIterator() = runBlocking<Unit> {
        val channel = Channel<Int>(Channel.UNLIMITED)
        //生产者
        val producer = GlobalScope.launch {
           for(i in 1..5) {
               channel.send(i * i)
               println("send ${i * i}")
           }
        }

        //消费者
        val consumer = GlobalScope.launch {
//            val iterator = channel.iterator()
//            while (iterator.hasNext()) {
//                val element = iterator.next()
//                println("receive $element")
//                delay(2000)
//            }

            for (element in channel) {
                println("receive $element")
                delay(2000)
            }
        }
        joinAll(producer, consumer)
    }
    //send 1
    //send 4
    //send 9
    //send 16
    //send 25
    //receive 1
    //receive 4
    //receive 9
    //receive 16
    //receive 25

    @Test
    fun testFastProducerChannel() = runBlocking<Unit> {
        val receiveChannel: ReceiveChannel<Int> = GlobalScope.produce<Int> {
            repeat(100) {
                delay(1000)
                send(it)
            }
        }

        val consumer = GlobalScope.launch {
            for(i in receiveChannel) {
                println("received: $i")
            }
        }
        consumer.join()
    }
    //received: 0
    //received: 1
    //received: 2
    //...

    @Test
    fun testFastConsumerChannel() = runBlocking<Unit> {
        val sendChannel: SendChannel<Int> = GlobalScope.actor<Int> {
            while (true) {
                val element = receive()
                println(element)
            }
        }

        val producer = GlobalScope.launch {
            for(i in 1..3) {
                sendChannel.send(i)
            }
        }
        producer.join()
    }
    //1
    //2
    //3

    @Test
    fun testCloseChannel() = runBlocking<Unit> {
        val channel = Channel<Int>(3)
        //生产者
        val producer = GlobalScope.launch {
            List(3) {
                channel.send(it)
                println("send $it")
            }
            channel.close()
            println(("close channel " +
                    "| - ClosedForSend: ${channel.isClosedForSend} " +
                    "| - ClosedForReceive: ${channel.isClosedForReceive}").trimMargin())
        }

        //消费者
        val consumer = GlobalScope.launch {
            for (element in channel) {
                println("receive $element")
                delay(1000)
            }
            println(("close channel " +
                    "| - ClosedForSend: ${channel.isClosedForSend} " +
                    "| - ClosedForReceive: ${channel.isClosedForReceive}").trimMargin())
        }
        joinAll(producer, consumer)
    }
    //send 0
    //receive 0
    //send 1
    //send 2
    //close channel | - ClosedForSend: true | - ClosedForReceive: false
    //receive 1
    //receive 2
    //close channel | - ClosedForSend: true | - ClosedForReceive: true

    @Test
    fun testBroadcastChannel() = runBlocking<Unit> {
        //val broadcastChannel = BroadcastChannel<Int>(Channel.BUFFERED)
        //普通channel可以转换为BroadcastChannel
        val channel = Channel<Int>()
        val broadcastChannel = channel.broadcast(3)
        val producer = GlobalScope.launch {
            List(3) {
                delay(100)
                broadcastChannel.send(it)
            }
            broadcastChannel.close()
        }

        List(3) { index ->
            GlobalScope.launch {
                val receiveChannel = broadcastChannel.openSubscription()
                for(i in receiveChannel) {
                    println("[#$index] received: $i")
                }
            }
        }.joinAll()
    }
    //[#0] received: 0
    //[#2] received: 0
    //[#1] received: 0
    //[#0] received: 1
    //[#2] received: 1
    //[#1] received: 1
    //[#0] received: 2
    //[#1] received: 2
    //[#2] received: 2
}