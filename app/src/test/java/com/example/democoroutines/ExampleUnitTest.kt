package com.example.democoroutines

import kotlinx.coroutines.*
import org.junit.Test

import org.junit.Assert.*
import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    // Coroutines are light-weight threads

    //Coroutine basics
    @Test
    fun testWithGlobalScope() {
        GlobalScope.launch {
            // meaning that the lifetime of the new coroutine is limited only by the lifetime of the whole application.
            delay(1000L)
            println("World! ${Thread.currentThread().name}")
        }
        println("Hello,${Thread.currentThread().name}")
        Thread.sleep(2000L)
    }

    //Bridging blocking and non-blocking worlds

    @Test
    fun testWithRunBlocking() {
        GlobalScope.launch {
            // launch a new coroutine in background and continue
            println("World! ${Thread.currentThread().name}")
        }
        println("Hello, ${Thread.currentThread().name}") // main thread continues here immediately
        runBlocking {
            // but this expression blocks the main thread
            delay(2000L)  // ... while we delay for 2 seconds to keep JVM alive
        }
    }


    @Test
    fun main() = runBlocking<Unit> {
        // start main coroutine
        GlobalScope.launch {
            // launch a new coroutine in background and continue
            delay(1000L)
            println("World!  ${Thread.currentThread().name}")
        }
        println("Hello,  ${Thread.currentThread().name}") // main coroutine continues here immediately
        delay(2000L)      // delaying for 2 seconds to keep JVM alive
    }

    //Waiting for a job

    @Test
    fun mainWaiting() = runBlocking {
        //sampleStart
        val job = GlobalScope.launch {
            // launch a new coroutine and keep a reference to its Job
            delay(1000L)
            println("World! ${Thread.currentThread().name}")
            launch {
                println("Children! ${Thread.currentThread().name}")
            }
        }
        println("Hello, ${Thread.currentThread().name}")
        job.join() // wait until child coroutine completes
        println("End, ${Thread.currentThread().name}")
//sampleEnd
    }


    //Structured concurrency
    @Test
    fun mainConcurrency() = runBlocking {
        // this: CoroutineScope
        launch {
            // launch a new coroutine in the scope of runBlocking
            delay(1000L)
            println("World!")
        }
        println("Hello,")
    }

    //Scope builder
    @Test
    fun mainScope() = runBlocking {
        // this: CoroutineScope
        launch {
            println("Task from runBlocking ${Thread.currentThread().name}")
        }

        coroutineScope {
            // Creates a coroutine scope
            launch {

                println("Task from nested launch ${Thread.currentThread().name}")
            }


            println("Task from coroutine scope ${Thread.currentThread().name}") // This line will be printed before the nested launch
        }

        println("Coroutine scope is over ${Thread.currentThread().name}") // This line is not printed until the nested launch completes
    }
    //Extract function refactoring

    //Coroutines ARE light-weight

    @Test
    fun mainCoroutines() = runBlocking {
        repeat(100_000) {
            // launch a lot of coroutines
            launch {
                delay(1000L)
                print(".")
            }
        }
    }

    /*@Test
    fun mainThread() = Thread {
        Thread.sleep(1000L)
        print(".")
    }*/

    //Global coroutines are like daemon threads

    @Test
    fun mainDaemon() = runBlocking {
        //sampleStart
        GlobalScope.launch(Dispatchers.Unconfined) {
            repeat(1000) { i ->
                println("I'm sleeping $i ... ${Thread.currentThread().name}")
            }
        }
        delay(1300L) // just quit after delay
//sampleEnd
    }

    //Cancellation and timeouts
    @Test
    fun mainCancellation() = runBlocking {
        //sampleStart
        val job = launch {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancel() // cancels the job
        job.join() // waits for job's completion
        println("main: Now I can quit.")
//sampleEnd
    }

    @Test
    fun mainCancellationAndJoin() = runBlocking {
        //sampleStart
        val job = launch {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
//sampleEnd
    }

    //Cancellation is cooperative
    @Test
    fun mainCooperative() = runBlocking {
        //sampleStart
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (i < 5) { // computation loop, just wastes CPU
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancel()
        job.join()
        //job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
//sampleEnd
    }

    //Making computation code cancellable
    @Test
    fun mainComputation() = runBlocking {
        //sampleStart
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (isActive) { // cancellable computation loop
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
//sampleEnd
    }

    //Closing resources with finally
    @Test
    fun mainFinally() {
        runBlocking {
            //sampleStart
            val job = launch {
                try {
                    repeat(1000) { i ->
                        println("job: I'm sleeping $i ...")
                        delay(500L)
                    }
                } finally {
                    println("job: I'm running finally")
                }
            }
            delay(1300L) // delay a bit
            println("main: I'm tired of waiting!")
            job.cancelAndJoin() // cancels the job and waits for its completion
            println("main: Now I can quit.")
//sampleEnd
        }
        println("main: Now I can quit.fldashfla;sjfl")
    }

    // Run non-cancellable block
    @Test
    fun mainNonCancellable() = runBlocking {
        //sampleStart
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job: I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {
                withContext(NonCancellable) {
                    println("job: I'm running finally")
                    delay(2000L)
                    println("job: And I've just delayed for 1 sec because I'm non-cancellable")
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
//sampleEnd
    }

    //Timeout
    @Test
    fun mainTimeout() = runBlocking {
        //sampleStart
        withTimeout(1300L) {
            try {
                repeat(1000) { i ->
                    println("I'm sleeping $i ...")
                    delay(500L)
                }
            }catch (ex:Exception){
                println("main: Now I can quit. ${ex.message}")
            }

        }
//sampleEnd
    }

    @Test
    fun mainTimeoutOrNull() = runBlocking {
        //sampleStart
        val result = withTimeoutOrNull(1300L) {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
            "Done" // will get cancelled before it produces this result
        }
        println("Result is $result")
//sampleEnd
    }


    /// Demo
    @Test
    fun testHello() {
        runBlocking {
            println("Start on main thread")
            val job = launch(Dispatchers.Default) {
                delay(5000)
                println("Hello from coroutine")
            }

            println("Hello from main thread")
            job.join()
            println("Stop on main thread")
        }
    }

    @Test
    fun testThread() {
        val time = measureTimeMillis {
            val jobs = List(10_000) {
                thread(start = true) {
                    Thread.sleep(1000L)
                    print(".")
                }
            }
            jobs.forEach { it.join() }
        }
        println()
        print(time)
    }

    @Test
    fun testCoroutines() {
        runBlocking<Unit> {
            val time = measureTimeMillis {
                val jobs = List(10_000) {
                    launch(Dispatchers.Default) {
                        delay(1000L)
                        print(".")
                    }
                }
                jobs.forEach { it.join() }
            }
            println()
            println(time)
        }
    }

    @Test
    fun testAsyncAWait() {
        runBlocking {
            val deferred = (1..60_000).map { n ->
                async(Dispatchers.Default) {
                    n
                }
            }
            runBlocking {
                val sum = deferred.sumBy { it.await() }
                println("Sum: $sum")
            }
        }
    }

    @Test
    fun testSuspend() {
        runBlocking {
            val deferred = (1..1_000_000).map { n ->
                async(Dispatchers.Default) {
                    doWork(n)
                }
            }
        }
    }

    suspend fun doWork(n: Int): Int {
        delay(50)
        return n
    }
}



