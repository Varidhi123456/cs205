package com.example.cookingosgame
import java.util.concurrent.ExecutorService
//executorservice is like a group of reusable background workers -> threads
class Stove(val id: Int, private val threadPool: ExecutorService) {

    //each stove has a unique id and linked to a shared thread pool (where a group of chefs help you cook in a sense)

    @Volatile
    var currentProcess: DishProcess? = null
        private set


    // run one dish process at a time for each stove.
    // when a player 'drops' a dish onto the stove, it starts "cooking" simulating process execution
    // once its cooked "burst time" it marks the process as finished and free
    // i just add print statements in case we need to check/debug
    private fun simulateCooking(process: DishProcess) {
        try {
            if (process.state == ProcessState.STALE) {
                println("Dish '${process.name}' is stale and cannot continue cooking on Stove $id")
                return
            }
            Thread.sleep(process.burstTime)
            process.state = ProcessState.FINISHED
            println("Dish '${process.name}' finished cooking on Stove $id")
        } catch (e: InterruptedException) {
            process.state = ProcessState.STALE
            println("Dish '${process.name}' was interrupted on Stove $id")
        }

    }

    //assign the dish to the stove to start cooking and use @synchronized to prevent race condition
    @Synchronized
    fun assignProcess(process: DishProcess) {
        if (!isFree()) return

        if (process.state == ProcessState.STALE) {
            return
        }

        currentProcess = process //set dish
        process.state = ProcessState.RUNNING //change state

        threadPool.execute {
            simulateCooking(process) // simulate cpu
        }
    }

    fun manuallyRemoveProcess() {
        if (currentProcess?.state == ProcessState.FINISHED) {
            currentProcess = null
        }
        if (currentProcess?.state == ProcessState.BURNT) {
            currentProcess = null
        }
    }
    fun isFree(): Boolean = currentProcess == null
}