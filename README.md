# `Kotlin`协程学习笔记

[TOC]

## 一、概念相关

### 1.1 协程

#### 1.1.1 协程是什么？

   携程基于线程，是轻量级线程。

	* 协程让异步逻辑同步化，杜绝回调地狱；
	* 协程最核心的点就是函数或者一段程序能够被挂起，稍后再在挂起的位置恢复。

#### 1.1.2 协程在`Android`中解决什么问题？

	* 处理耗时任务，这种任务常常会阻塞主线程；
	* 保证主线程安全，即确保安全地从主线程调用任何`suspend`函数。

#### 1.1.3 协程的挂起与恢复

   常规函数基本操作包括：`invoke`(或`call`)和`return`，协程新增了`suspend`和`resume`

    * `suspend`也称为 挂起或暂停，用于暂停执行当前协程，并保存所有局部变量；
    * `resume`用于让已经暂停的协程从其暂停处继续执行。

#### 1.1.4 挂起函数

	* 使用`suspend`关键字修饰的函数叫做挂起函数；
	* 挂起函数只能在 **协程体内** 或 **其它挂起函数内** 调用。

#### 1.1.5 挂起与阻塞
   * 挂起：会记录当前的挂起点，待到时机到达时执行（连续点击会多次响应）；
   * 阻塞：会一直等待，不能干其它事情（连续点击仅会响应第一次，可能会引起`ANR`）。

#### 1.1.6 协程的两部分

   * 基础设施层：标准的协程`API`，主要对协程提供了概念和语义上最基本的支持；

   ```kotlin
       // Kotlin协程基础框架层
       private fun baseCoroutine() {
           //协程体
           val continuation = suspend { 
               5
           }.createCoroutine(object: Continuation<Int>{
               override val context: CoroutineContext = EmptyCoroutineContext
   
               override fun resumeWith(result: Result<Int>) {
                   println("Coroutine End: $result")
               }
           })
           //执行
           continuation.resume(Unit)
       }
   ```

   * 业务框架层：协程的上层框架支持。

#### 1.1.7 调度器

   所有协程必须在调度器中运行，即使它们在主线程上运行也是如此

   * **`Dispatchers.Main`**：`Android`上的主线程，用来处理`UI`交互和一些轻量级任务（调用`suspend`函数、调用`UI`函数、更新`LiveData`）；
   * **`Dispatchers.IO`**：非主线程，专为磁盘和网络进行了优化（数据库、文件读写、网络处理）；
   * **`Dispatchers.Default`**：非主线程，专为`CPU`密集型任务进行了优化（数组排序、`JSON`数据解析、处理差异判断）。

#### 1.1.8 任务泄漏

   当某个协程任务丢失、无法追踪会导致内存、`CPU`、磁盘等资源浪费，甚至发送一个无用的网络请求，这种情况称为 **任务泄漏** 。

   为了能够避免协程泄漏，`Kotlin`引入了 **结构化并发机制 ** 。

#### 1.1.9 结构化并发

   使用结构化并发可以做到：

   * 取消任务：当某项任务不再需要时取消它；
   * 追踪任务：当任务正在执行时，追踪它；
   * 发出错误信号：当协程失败时，发出错误信号表明有错误发生。

#### 1.1.10 `CoroutineScope`

    定义协程必须指定其`CoroutineScope`，它会跟踪所有协程，同样它还可以 **取消由它所启动的所有协程** ，常有的`API`有：
    
    * `GlobalScope`：生命周期是`process`级别的，即使`Activity`或`Fragment`已经被销毁，协程仍然在执行；
    * `MainScope`：在`Activity`中使用，可以在`onDestroy()`中取消协程；
    * `viewModelScope`：只能在`ViewModel`中使用，绑定`ViewModel`的生命周期；
    * `lifecycleScope`：只能在`Activity`、`Fragment`中使用，会绑定`Activity`和`Fragment`的生命周期。

## 二、协程的创建与取消

### 2.1 协程的启动

#### 2.1.1 协程构建器

   `launch`与`async`构建器都用来启动新协程

   * `launch`：返回一个`Job`并且不附带任何结果值；
   * `async`：返回一个`Deferred`，`Deferred`也是一个`Job`，可以使用`.await()`在一个延期的值上得到它的最终结果。

   ```kotlin
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
   ```

   等待一个作业：

   * `join`与`await`
   * 组合并发

   ```kotlin
   //`join`与`await`-------------------------------------------------------------------
       @Test
       fun testCoroutineJoin() = runBlocking {
           val job1 = launch {
               delay(2000)
               println("One")
           }
           job1.join() //等待job1启动完毕之后再启动job2和job3
   
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
   
   //组合与并发------------------------------------------------------------------------
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
   ```

   #### 2.1.2 协程的启动模式

* `DEFAULT`：协程创建后，立即开始调度（不一定立即执行），在调度前如果协程被取消，其将直接进入取消状态；

* `ATOMIC`：协程创建后，立即开始调度（不一定立即执行），协程执行到第一个挂起点之前不响应取消；

* `LAZY`：只有协程被需要时，包括主动调用协程的`start`、`join`或者`await`等函数时才会开始调度，如果调度前就被取消，那么该协程将直接进入异常结束状态；

* `UNDISPATCHED`：协程创建后立即在**当前函数调用栈**中执行，直到遇到第一个真正挂起的点。

  ```kotlin
  
  @Test
  fun testStartMode() = runBlocking {
      val job2 = async(context = Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
          println("thread1: ${Thread.currentThread().name}")
          delay(1000)
          print("thread2: ${Thread.currentThread().name}")
      }
      job2.await()
  }
  //thread1: Test worker @coroutine#3
  //CoroutineStart.DEFAULT
  //thread1: DefaultDispatcher-worker-1 @coroutine#3
  ```

#### 2.1.3 协程的作用域构建器

`coroutineScope`与`runBlocking`：

* `runBlocking`是常规函数，而`coroutineScope`是挂起函数；
* 它们都会等待其协程体以及所以子协程结束，主要区别在于`runBlocking`方法会阻塞当前线程来等待，而`coroutineScope`只是挂起，会释放底层线程用于其它用途。

`coroutineScope`与`supervisorScope`：

* `coroutineScope`：一个协程失败了，所以其它兄弟协程（未被调度的--个人见解）也会被取消；

* `supervisorScope`：一个协程失败了，不会影响其它兄弟协程。

  ```kotlin
  @Test
  fun testCoroutineScopeBuilder() = runBlocking {
      coroutineScope {
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
  ```

#### 2.1.4 `Job`对象

* 对于每一个创建的协程（通过`launch`或者`async`），会返回一个`Job`实例，改实例是协程的唯一标识，并且负责管理协程的生命周期；
* 一个任务可以包含一系列状态：新创建（`New`）、活跃（`Active`）、完成中（`Completing`）、已完成（`Completed`）、取消中（`Cancelling`）和已取消（`Cancelled`），虽然我们无法直接访问这些状态，但是我们可以访问`Job`的属性：`isActive`、`isCancelled`和`isCompleted`。

#### 2.1.5 `Job`的生命周期

如果协程处于活跃状态，协程运行出错或者调用`job.cancel()`都会将当前任务置为取消中（`Cancelling`）状态（`isActive = false, is Cancelled = true`），当所有的子协程都完成后，协程会进入已取消（`Cancelled`）状态，此时`isCompleted = true`。

![image-20220115092714258](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202201150927306.png)

### 2.2 协程的取消

#### 2.2.1 协程的取消

* 取消作用域会取消它的子协程

  ```kotlin
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
  ```

* 被取消的子协程并不会影响其余兄弟协程

  ```kotlin
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
  ```

* 协程通过抛出一个特殊的异常`CancellationException`来处理取消操作

  ```kotlin
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
      job1.cancel(CancellationException("取消"))
      //kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled
      //job1.join() //阻塞父协程
      
      job1.cancelAndJoin()
  }
  ```

* 所有`kotlinx.coroutines`中的挂起函数（`withContext`、`delay`等）都是可以取消的

#### 2.2.2 `CPU`密集型任务取消

* `isActive`是一个可以被使用在`CoroutineScope`中的扩展属性，检查`Job`是否处于活跃状态

  ```kotlin
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
  ```

* `ensureActive()`：如果`job`处于非活跃状态，这个方法会立即抛出异常

  ```kotlin
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
  ```

* `yield`函数会检查所在协程的状态，如果已经取消，则抛出`CancellationException`予以响应；此外它还会尝试让出线程的执行权，给其它协程提供执行机会

  ```kotlin
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
  ```

#### 2.2.3 协程取消的副作用

* 在`finally`中释放资源

  ```kotlin
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
  ```

* `use`函数：该函数只能被实现了`Closeable`的对象使用，程序结束时会自动调用`close`方法，适合文件对象

  ```kotlin
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
  ```

#### 2.2.4 不能取消的任务

处于取消中状态的协程不能够挂起（运行不能取消的代码），当协程被取消后需要调用挂起函数，我们需要将清理任务的代码放置于`NonCancellable CoroutineContext`中，这样会挂起运行中的代码，并保持协程的取消中状态直到任务处理完成

```kotlin
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
```

#### 2.2.5 超时任务

很多情况下取消一个协程的理由是它有可能超时，`withTimeoutOrNull`通过返回`null`来进行超时操作，从而替代抛出一个异常

```kotlin
@Test
fun testDealWithTimeout() = runBlocking<Unit> {
    //    withTimeout(1300) {
    //        repeat(1000) { i ->
    //            println("job: I'm sleeping $i ...")
    //            delay(500)
    //        }
    //    }
    //job: I'm sleeping 0 ...
    //job: I'm sleeping 1 ...
    //job: I'm sleeping 2 ...
    //Timed out waiting for 1300 ms
    //kotlinx.coroutines.TimeoutCancellationException: Timed out waiting for 1300 ms

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
```

## 三、协程的异常处理

### 3.1 协程的上下文

#### 3.1.1 协程的上下文是什么？

`CoroutineContext`是一组用于定义协程行为的元素，它由如下几项构成：

* `Job`：控制协程的生命周期
* `CoroutineDispatcher`：向合适的线程分发任务
* `CoroutineName`：协程的名称，调试的时候很有用
* `CoroutineExceptionHandler`：处理未被捕获的异常

#### 3.1.2 组合上下文的元素

有时候我们需要在协程上下文中定义多个元素，我们可以使用+操作符来实现，比如我们可以显式指定一个调度器来启动协程并且同时显式指定一个命名：

```kotlin
@Test
fun testCoroutineContext() = runBlocking<Unit> {
    launch(Dispatchers.Default + CoroutineName("test")) {
        println("I'm working in thread ${Thread.currentThread().name}")
    }
}
//I'm working in thread DefaultDispatcher-worker-2 @test#2
```

#### 3.1.3 协程上下文的继承

对于新创建的协程，它的`CoroutineContext`会包含一个全新的`Job`实例，它会帮助我们控制协程的生命周期，而 **剩下的元素会从`CoroutineContext`的父类继承** ，该父类可能是另外一个协程或者创建该协程的`CoroutineScope`：

```kotlin
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
```

协程的上下文 = 默认值 + 继承的`CoroutineContext` + 参数

*  一些元素包含默认值：`Dispatchers.Default`是默认的`CoroutineDispatcher`，以及`coroutine`作为默认的`CoroutineName`；
* 继承的`CoroutineContext`是`CoroutineScope`或者其父协程的`CoroutineContext`；
* 传入协程构建器的参数的优先级高于继承的上下文参数，因此会覆盖对应的参数值。

```kotlin
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
```

最终的父级`CoroutineContext`会内含`Dispatchers.IO`而不是`scope`对象里的`Dispatchers.Main`，因为它被协程的构建器里的参数覆盖了，此外，注意一下父级`CoroutineContext`里的`Job`是`scope`对象的`Job`（红色），二新的`Job`实例（绿色）会赋值给新的协程的`CoroutineContext`。

![image-20220118200623761](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202201182006861.png)

### 3.2 协程的异常处理

#### 3.2.1 异常处理的必要性

当应用出现一些意外情况时，给用户提供合适的体验非常重要，一方面目睹应用崩溃是个很糟糕的体验，另一方面，在用户操作失败时，也必须要能给出正确的提示信息。

#### 3.2.2 异常的传播

协程构建器有两种形式：**自动传播异常**（`launch`与`actor`）、**向用户暴露异常**（`async`与`produce`）,当这些构建器用于创建一个 **根协程** 时（该协程不是另一个协程的子协程），前者这类构建器的异常会在它发生的第一时间被抛出，而后者则依赖用户来最终消费异常，例如通过`await`或`receive`。

```kotlin
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
```

#### 3.2.3 非根协程的异常

其它协程所创建的协程中，产生的异常总是会被传播。

```kotlin
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
```

#### 3.2.4 异常的传播特性

当一个协程由于一个异常而运行失败时，它会传播这个异常并传递给它的父级，接下来父级会进行下面几步操作：

* 取消它自己所有的子集
* 取消它自己
* 将异常传播并传递给它的父级

![image-20220118202944306](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202201182029365.png)

#### 3.2.5 `SupervisorJob`

使用`SupervisorJob`时，一个子协程的运行失败不会影响到其它子协程，`SupervisorJob`不会传播异常给它的父级，它会 **让子协程自己处理异常**。

这种需求常见于在作用域定义作业的`UI`组件，如果任何一个`UI`的子作业执行失败了，它并不总是有必要取消整个`UI`组件，但是如果`UI`组件被销毁了，由于它的结果不再被需要了，它就有必要使所有的子作业执行失败。

```kotlin
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
```

#### 3.2.6 `supervisorScope`

当作业自身执行失败的时候，所有子作业将会被全部取消

```kotlin
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
```

#### 3.2.7 异常的捕获

* 使用`CoroutineExceptionHandler`对协程的异常进行捕获

* 以下的条件被满足时，异常就会被捕获：

  > **时机**：异常时被自动抛出异常的协程所抛出的（使用`launch`而不是`async`时）；
  >
  > **位置**：在`CoroutineScope`的`CoroutineContext`中或在一个根协程（`CoroutineScope`或者`supervisorScope`的直接子协程）中。

```kotlin
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
```

#### 3.2.8 `Android`中全局异常处理

全局异常处理器可以获取到所有协程未处理的未捕获异常，不过它并不能对异常进行捕获，虽然  **不能阻止程序崩溃** ，全局异常处理器在程序调试和异常上报等场景中仍然有非常大的用处：

我们需要在`classpath`下面创建`META-INF/services`目录，并在其中创建一个名为`kotlinx.coroutines.CoroutineExceptionHandler`的文件，文件内容就是我们的全局异常处理器的全类名。

#### 3.2.9 取消与异常

* 取消与异常密切相关，协程内部使用`CancellationException`来进行取消，这个异常会被忽略；
* 当子协程被取消时，不会取消它的父协程；
* 如果一个协程遇到了`CancellationException`以外的异常，它将使用该异常取消它的父协程；当父协程的所有子协程都结束后，异常才会被父协程处理。

```kotlin
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
        yield()
        println("Parent is not cancelled")
    }
    job.join()
}
//Cancelling child
//Child is cancelled
//Parent is not cancelled
```

```kotlin
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
```

#### 3.2.10 异常聚合

当协程的多个子协程因为异常而失败时，一般情况下取第一个异常进行处理。在第一个异常之后发生的所有其它异常，都将被绑定到第一个异常之上。

```kotlin
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
```

## 四、`Flow`-- 异步流

### 4.1 认识`Flow`

#### 4.1.1 如何表示多个值？

挂起函数可以异步地返回单个值，但是该如何异步返回多个计算好的值呢？

异步返回多个值的方案：

> 1. 集合
> 2. 序列
> 3. 挂起函数
> 4. `Flow`

#### 4.1.2 `Flow`与其它方式的区别

* 名为`flow`的`Flow`类型构建器函数；
* `flow{...}`构建块中的代码可以挂起；
* 函数`simple`不再标有`suspend`修饰符；
* 流使用`emit`函数发射值；
* 流使用`collect`函数收集值。

![image-20220125210317738](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202201252103888.png)

#### 4.1.3 `Flow`应用--文件下载

在`Android`中，文件下载是`Flow`的一个非常典型的应用：

![image-20220125210501802](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202201252105867.png)

#### 4.1.4 冷流

`Flow`是一种类似于序列的**冷流**，`flow`构建器中的代码直到流被收集的时候才运行。

```kotlin
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
    flow.collect {value -> println(value)}
    println("Calling collect again...")
    flow.collect {value -> println(value)}
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
```

#### 4.1.5 流的连续性

* 流的每次单独收集都是按顺序执行的，触发使用特殊操作符；
* 从上游到下游每个过渡操作符都会处理每个发射出的值，然后再交给末端操作符。

```kotlin
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
```

#### 4.1.6 流构建器

* `flowOf`构建器定义了一个发射固定值集的流；
* 使用`.asFlow()`扩展函数，可以将各种集合与序列转换为流。

```kotlin
@Test
fun testFlowBuilder() = runBlocking<Unit> {
    //        flowOf("one", "two", "three")
    //            .onEach { delay(1000) }
    //            .collect { println(it) }

    (1..3).asFlow()
    .collect{ println(it)}
}
```

#### 4.1.7 流的上下文

* 流的收集总是在调用协程的上下文中发生，流的该属性称为**上下文保存**；

  ```kotlin
  fun simpleFlow3() = flow<Int> {
      println("Flow started ${Thread.currentThread().name}")
      for (i in 1..3) {
          delay(1000)
          emit(i) //发射，产生一个元素
      }
  }
  
  @Test
  fun testFlowContext() = runBlocking<Unit> {
      simpleFlow3().collect{ println("Collected $it ${Thread.currentThread().name}")}
  }
  //Flow started Test worker @coroutine#1
  //Collected 1 Test worker @coroutine#1
  //Collected 2 Test worker @coroutine#1
  //Collected 3 Test worker @coroutine#1
  ```

* `flow{...}`构建器中的代码必须遵循上下文保存属性，并且不允许从其它上下文中发射（`emit`）；

* **`flowOn`操作符** 用于更改流发射的上下文。

  ```kotlin
  fun simpleFlow4() = flow<Int> {
      println("Flow started ${Thread.currentThread().name}")
      for (i in 1..3) {
          delay(1000)
          emit(i) //发射，产生一个元素
      }
  }.flowOn(Dispatchers.Default)
  
  @Test
  fun testFlowOn() = runBlocking<Unit> {
      simpleFlow4().collect{ println("Collected $it ${Thread.currentThread().name}")}
  }
  //Flow started DefaultDispatcher-worker-1 @coroutine#2
  //Collected 1 Test worker @coroutine#1
  //Collected 2 Test worker @coroutine#1
  //Collected 3 Test worker @coroutine#1
  ```

#### 4.1.8 启动流

使用`launchIn`代替`collect`我们可以在单独的协程中启动流的收集。

```kotlin
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
```

#### 4.1.9 流的取消

流采用与协程同样的协作取消，像往常一样，流的收集可以是当流在一个可取消的挂起函数（例如`delay`）中挂起的时候取消。

```kotlin
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
        simpleFlow5().collect { println(it)}
    }
    println("Done")
}
//1
//Emitting 1
//2
//Emitting 2
//Done
```

#### 4.1.10 流的取消检测

* 为方便起见，流构建器对每个发射值进行附加的`ensureActive`检测以进行取消，这意味着从`flow{...}`发出的繁忙循环是可以取消的；

  ```kotlin
  @Test
  fun testFlowCancelCheck1() = runBlocking<Unit> {
      flow<Int> {
          for (i in 1..5) {
              emit(i)
              println("Emitting $i")
          }
      }.collect{
          println(it)
          if(it == 3) {
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
  ```

* 出于性能原因，大多数其它流操作不会自行执行其它取消检测，在协程处于繁忙循环的情况下，必须明确检测是否取消；

* 通过`cancellable`操作符来执行此操作。

  ```kotlin
  @Test
  fun testFlowCancelCheck2() = runBlocking<Unit> {
      (1..5).asFlow()
      .cancellable() //不加这个虽然也会报取消异常，但是依然会打印4、5，因为繁忙循环不会自行执行其它取消检测
      .collect {
          println(it)
          if(it == 3) {
              cancel()
          }
      }
  }
  //1
  //2
  //3
  //BlockingCoroutine was cancelled
  //kotlinx.coroutines.JobCancellationException: BlockingCoroutine was cancelled;
  ```

#### 4.1.11 背压

产生原因：生产效率 > 消费效率

处理方式：

* `buffer()`：并发运行流中发射元素的代码；
* `conflate()`：合并发射项，不对每个值进行处理；
* `collectLatest()`：取消并重新发射最后一个值；
* 当必须更改`CoroutineDispatcher`时，`flowOn`操作符使用了相同的缓冲机制，但是`buffer`函数显式地请求缓冲而 **不改变执行上下文** 。

### 4.2 操作符

#### 4.2.1 过度流操作符(转换操作符)`map/transform`

* 可以使用操作符转换流，就像使用集合与序列一样；
* 过渡操作符应用于上游流，并返回下游流；
* 这些操作符也是冷操作符，就像流一样，这类操作符本身不是挂起函数；
* 它运行速度很快，返回新的转换流的定义。

**`map`**

```kotlin
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
```

**`transform`**

```kotlin
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
```

#### 4.2.2 限长操作符`take`

```kotlin
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
```

#### 4.2.3 末端流操作符

末端流操作符是在流上用于 **启动流收集的挂起函数** 。`collect`是最基础的末端操作符，但是还有另外一些更方便使用的末端操作符：

* 转化为各种集合，例如`toList`与`toSet`;

* 获取第一个（`first`）值与确保流发射单个（`single`）值的操作；

* 使用`reduce`与`fold`将流规约到单个值。

  ```kotlin
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
  ```

#### 4.2.4 组合流操作符`zip`

  就像`Kotlin`标准库中的`Sequence.zip`扩展函数一样，流拥有一个`zip`操作符用于组合两个流中的相关值：

  ```kotlin
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
  ```

#### 4.2.5 展平流操作符

流表示异步接收的值序列，所以很容易遇到这样的情况：每个值都会触发对另一个值序列的请求，然而，由于流具有异步的性质，因此需要不同的展平模式，为此存在一系列的流展平操作符：

* `flatMapConcat`连接模式

  ```kotlin
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
  ```

* `flatMapMerge`合并模式

  ```kotlin
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
  ```

* `flatMapLatest`最新展平模式

  ```kotlin
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
  ```

![image-20220210200200162](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202202102002258.png)

### 4.3 异常处理

#### 4.3.1 流的异常处理

当运算符中的发射器或代码抛出异常时，有几种处理异常的方法：

* `try/catch`块 --> 处理下游异常

  ```kotlin
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
  ```

* `catch`函数 -->处理上游异常

  ```kotlin
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
  ```

#### 4.3.2 流的完成

当流收集完成时（普通情况或异常情况），它可能需要执行一个动作

* 命令式`finally`块

  ```kotlin
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
  ```

* `onCompletion`声明式处理 --> 可以拿到上游/下游的异常信息

  ```kotlin
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
  ```

## 五、通道-多路复用-并发安全

### 5.1 通道

#### 5.1.1 认识通道

`Channel`实际上是一个并发安全的队列，它可以用来连接协程，实现不同协程的通信。

![image-20220211143505839](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202202111435907.png)

```kotlin
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
```

#### 5.1.2 `Channel`的容量

`Channel`实际上就是一个队列，队列中一定存在缓冲区，那么一旦这个缓冲区满了，并且也一直没有人调用`receive`取走函数，`send`就需要挂起。故意让接收端的节奏放慢，发现`send`总是会挂起，直到`receive`之后才会继续往下走（代码同5.1.1）。

#### 5.1.3 迭代`Channel`

`Channel`本身确实像序列，所以我们在读取的时候可以直接获取一个`Channel`的`iterator`.

```kotlin
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
```

#### 5.1.4 `produce`与`actor`

`produce`和`actor`是构造生产者和消费者的便捷方法，我们可以通过`produce`方法启动一个生产者协程，并返回一个`ReceiveChannel`，其它协程就可以用这个`Channel`来接收数据了；反过来我们可以用`actor`启动一个消费者协程。

```kotlin
@Test
fun testFastProducerChannel() = runBlocking<Unit> {
    val receiveChannel: ReceiveChannel<Int> = GlobalScope.produce {
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
```

#### 5.1.5 `Channel`的关闭

* `produce`和`actor`返回的`Channel`都会随着对应的协程执行完毕而关闭，也正是这样，`Channel`才被称为 **热数据流** ；
* 对于一个`Channel`，如果我们调用了它的`close`方法，它会立即停止接收新元素，也就是说这时它的 **`isClosedForSend`** 会立即返回`true`；而由于`Channel`缓冲区的存在，这时候可能还有一些元素没有被处理完，因此要等所有的元素都被读取之后 **`isClosedForReceive`** 才会返回`true`；
* `Channel`的生命周期最好由主导方来维护，建议 **由主导的一方实现关闭** 。

```kotlin
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
```

#### 5.1.6 `BroadcastChannel`

前面提到，发送端和接收端在`Channel`中存在一对多的情形，从数据处理本身来讲，虽然有多个接收端，但是同一个元素只会被一个接收端读到，但广播则不然： **多个接收端不存在互斥行为** 。

![微信截图_20220211174233](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202202111744402.png)

```kotlin
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
```

### 5.2 `select` - 多路复用

#### 5.2.1 什么是多路复用

数据通信系统或计算机网络系统中，传输媒体的带宽或容量往往会大于传输单一信号的需求，为了有效地利用通信线路，希望 **一个信道同时传输多路信号** ，这就是所谓的多路复用技术（`Multiplexing`）。

![image-20220211175832001](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202202111758076.png)

#### 5.2.2 复用多个`await`

两个`API`分别从网络和本地缓存获取数据，期望哪个先返回就先用哪个做展示。

![image-20220211180130587](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202202111801658.png)

```kotlin
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
```

#### 5.2.3 复用多个`Channel`

跟`await`类似，会接收到最快的那个`channel`消息。

```kotlin
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
```

#### 5.2.4 `SelectClause`

我们怎么知道哪些事件可以被`select`呢？其实所有能够被`select`的事件都是`SelectClauseN`类型，包括：

* `SelectClause0`：对应事件没有返回值，例如`join`没有返回值，那么`onJoin`就是`SelectClauseN`类型，使用时，`onJoin`的参数是一个无参函数；
* `SelectClause`：对应事件有返回值，签名的`onAwait`和`onReceive`都是此类情况；
* `SelectClause2`：对应事件有返回值，此外还需要一个额外的参数，例如`Channel.onSend`有两个参数，第一个是`Channel`数据类型的值，表示即将发送的值，第二个是发送成功时的回调参数。

如果我们想要确认挂起函数是否支持`select`，只需要查看其 **是否存在对应的`SelectClauseN`类型** 可回调即可。

```kotlin
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
```

#### 5.2.5 使用`Flow`实现多路复用

多数情况下，我可以通过构造合适的`Flow`来实现多路复用的效果。

```kotlin
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
```

### 5.3 并发安全

#### 5.3.1 不安全的并发访问

我们使用线程在解决并发问题的时候总是会遇到线程安全的问题，而`Java`平台上的`Kotlin`协程实现免不了存在并发调度的情况，因此线程安全同样值得留意。

```kotlin
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
```

#### 5.3.2 协程的并发工具

除了我们在线程中常用的解决并发问题的手段之外，协程框架也提供了一些并发安全的工具，包括：

* `Channel`：并发安全的消息通道，我们已经非常熟悉；
* `Mutex`：轻量级锁，它的`lock`和`unlock`从语义上与线程锁比较类似，之所以轻量是因为它在获取不到锁时不会阻塞线程，而是挂起等待锁的释放；
* `Semaphore`：轻量级信号量，信号量可以有多个，协程在获取到信号量后即可执行并发操作，当`Semaphore`的参数为1时，效果等价于`Mutex`。

```kotlin
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
```

#### 5.3.3 避免访问外部可变状态

编写函数时要求它不得访问外部状态，只能基于参数做运算，通过返回值提供运算结果。

```kotlin
@Test
fun testAvoidAccessOuterVariable() = runBlocking<Unit> {
    var count = 0
    val result = count + List(1000){
        GlobalScope.async { 1 }
    }.map { it.await() }.sum()
    println(result)
}
//1000
```

## 六、实践操作

### 6.1 文件下载

文件下载流程如下图所示：

![image-20220214204927888](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202202142049977.png)

核心代码如下：

```kotlin
object DownloadManager {
    fun download(url: String, file: File) : Flow<DownloadStatus> {
        return flow {
            val request = Request.Builder().url(url).get().build()
            val response = OkHttpClient.Builder().build().newCall(request).execute()
            if(response.isSuccessful) {
                response.body()!!.let { body ->
                    val total = body.contentLength()
                    //文件读写
                    file.outputStream().use { output ->
                        val input = body.byteStream()
                        var emittedProgress = 0L
                        input.copyTo(output) { bytesCopied ->
                            val progress = bytesCopied * 100 / total
                            if(progress - emittedProgress > 5) {
                                delay(100)
                                emit(DownloadStatus.Progress(progress.toInt()))
                                emittedProgress = progress
                            }
                        }
                    }
                }
                emit(DownloadStatus.Done(file))
            }else {
                throw IOException(response.toString())
            }
        }.catch {
            file.delete()
            it.printStackTrace()
            emit(DownloadStatus.Error(it))
        }.flowOn(Dispatchers.IO)
    }
}

// DownloadFragment
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { _ ->
            lifecycleScope.launchWhenCreated {
                context?.apply {
                    //val file = File(getExternalFilesDir(null)?.path + "/sty/", "beauty.JPG")
                    // /storage/emulated/0/Android/data/com.sty.kotlincoroutine/files/sty/beauty.JPG
                    val file = File(
                        Environment.getExternalStorageDirectory()?.path + "/sty/",
                        "beauty.JPG"
                    )
                    // /storage/emulated/0/sty/beauty.JPG
                    DownloadManager.download(URL, file).collect { status ->
                        when (status) {
                            is DownloadStatus.Progress -> {
                                mBinding.apply {
                                    progressBar.progress = status.value
                                    tvProgress.text = "${status.value}%"
                                }
                            }
                            is DownloadStatus.Error -> {
                                Toast.makeText(context, "下载错误", Toast.LENGTH_SHORT).show()
                            }
                            is DownloadStatus.Done -> {
                                mBinding.apply {
                                    progressBar.progress = 100
                                    tvProgress.text = "100%"
                                }
                                Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show()

                            }
                            else -> {
                                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
```

### 6.2 `Flow`与`Room`的应用

核心代码如下：

`UserFragment`

```kotlin
class UserFragment : Fragment() {
    private val viewModel by viewModels<UserViewModel>()

    private val mBinding: FragmentUserBinding by lazy {
        FragmentUserBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mBinding.apply {
            btnAdd.setOnClickListener {
                viewModel.insert(
                    etId.text.toString().toInt(),
                    etName.text.toString(),
                    etAge.text.toString().toInt()
                )
            }
        }

        context?.let {
            val adapter = UserAdapter(it)
            mBinding.rvList.adapter = adapter
            mBinding.rvList.layoutManager = LinearLayoutManager(it)
            lifecycleScope.launchWhenCreated {
                viewModel.getAll().collect { value ->
                    adapter.setData(value)
                }
            }
        }
    }
}
```

`UserViewModel`

```kotlin
class UserViewModel(app: Application): AndroidViewModel(app) {
    fun insert(uid: Int, name: String, age: Int) {
        viewModelScope.launch {
            val user = User(uid, name, age)
            AppDatabase.getInstance(getApplication())
                .userDao()
                .insert(user)
            Log.d("sty", "insert user: $user")
        }
    }

    fun getAll(): Flow<List<User>> {
        return AppDatabase.getInstance(getApplication())
            .userDao()
            .getAll()
            .catch { e -> e.printStackTrace() }
            .flowOn(Dispatchers.IO)
    }
}
```

`User`

```kotlin
@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "age") val age: Int
)
```

`UserDao`

```kotlin
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>
}
```

`AppDatabase`

```kotlin
@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "user")
                    .build().also{
                        instance = it
                    }
            }
        }
    }
}
```

