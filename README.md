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

