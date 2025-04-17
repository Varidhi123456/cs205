package com.example.cookingosgame
import android.content.Context
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import kotlin.concurrent.thread
import androidx.compose.runtime.mutableStateListOf

// GameManager handles core logic of our OS cooking simulator:
// - Initializes 4 stoves (threads) and manages dish processes
// - Simulates I/O wait and starvation using Scheduler
// - Spawns new dishes every 5 ticks (tick = 1 game second)
// - Player manually assigns dishes from readyQueue to stoves
// - Player removes dishes after cooking is finished

//this class would control the main game as i assume
//what we need to do is track all stove and queues
// manages cooking via threadpool
//updates scheduler logic as per tick
class GameManager {
    //queues for dishes (processes)
    val readyQueue = mutableStateListOf<DishProcess>()
    val waitingQueue = mutableStateListOf<DishProcess>()
    val burntQueue = mutableStateListOf<DishProcess>()
    val completedDishes = mutableStateListOf<DishProcess>() //this is for when the dish are cooked completely
    var point = 0 // track points
    var lives = 5 // initial lives
    private val allDishes = mutableListOf<DishProcess>()
    private var dishIdCounter = 1
    private var ticksPassed = 0


    /** reboot everything back to its initial values. */
    fun resetGame() {
        point = 0
        lives = 5
        readyQueue.clear()
        waitingQueue.clear()
        completedDishes.clear()
        burntQueue.clear()
        allDishes.clear()
        ticksPassed = 0
        dishIdCounter = 1
        stoves.forEach { it.manuallyRemoveProcess() }
    }

    //shared thread pool for cooking simulation so lets say 4 stove i have 4 worker/chefs
    private val threadPool: ExecutorService = Executors.newFixedThreadPool(4)
    //create the 4 CPU cores (stoves)
    val stoves = List(4){id->Stove(id, threadPool)}
    //scheduler track timing logic
    private val scheduler = Scheduler(readyQueue,waitingQueue)
    private val burnThresholdMs = 10000L

    fun generateNewDish(): DishProcess {
        val dish = DishProcess(
            id = dishIdCounter++,
            name = generateRandomDishName(),
            burstTime = (3000L..7000L).random(),         // Simulate 3–7 sec cook
            ioWaitTime = listOf(0L, 1000L, 2000L).random(),   // Random I/O wait
            priority = (1..5).random()
        )
        addNewDish(dish)
        allDishes.add(dish)
        return dish
    }

    private fun generateRandomDishName(): String {
        val names = listOf("Steak", "Bacon & Eggs", "Grilled Fish", "Pancakes")
        return names.random()
    }

    fun updateGameTick(timeDelta: Long, context: Context) {
        scheduler.update(timeDelta)
        ticksPassed += 1

        // spawn new dishes more frequently as game progresses (optional)
        val spawnInterval = 7

        // made maximum number of dishes in ready queue = 10
        if (ticksPassed % spawnInterval == 0 && readyQueue.size < 10) {
            println("New order incoming!") //debug
            generateNewDish()
        }

        // Check for burnt dishes
        for (stove in stoves) {
            val dish = stove.currentProcess
            if (dish != null && dish.state == ProcessState.FINISHED) {
                dish.timeSinceFinished += timeDelta

                // Vibrate the device once if the dish is done
                if (!dish.notified) {
                    dish.notified = true
                    VibratorService().vibrate(context)
                }

                if (dish.timeSinceFinished >= burnThresholdMs) {
                    println("Dish '${dish.name}' was burnt on Stove ${stove.id}!")
                    dish.state = ProcessState.BURNT
                }
            }
        }
    }

    fun addNewDish(dish: DishProcess) {
        if (dish.ioWaitTime > 0L) {
            dish.state = ProcessState.WAITING
            waitingQueue.add(dish)
        } else {
            dish.state = ProcessState.READY
            readyQueue.add(dish)
        }
    }

    //when player place to stove
    fun assignDishToStove(dish: DishProcess, stove: Stove) {
        if (stove.isFree()) {
            readyQueue.remove(dish)
            stove.assignProcess(dish)
        }
    }

    fun removeDishFromStove(stove: Stove) {
        val finishedDish = stove.currentProcess
        if (finishedDish?.state == ProcessState.FINISHED) {
            completedDishes.add(finishedDish)
            println("Dish '${finishedDish.name}' completed and served!")
        }
        if (finishedDish?.state == ProcessState.BURNT) {
            burntQueue.add(finishedDish)
            println("Dish '${finishedDish.name}' burnt!")
            loseLife("Burnt")
        }
        stove.manuallyRemoveProcess()
    }

    fun managePoints(stove: Stove) {
        val finishedDish = stove.currentProcess
        if (finishedDish?.state == ProcessState.FINISHED) {
            point = point + 100
        }
        if (finishedDish?.state == ProcessState.BURNT) {
            point = point - 200
        }

    }

    fun loseLife(reason: String) {
        if (lives > 0) {
            lives--
            println("Lost a life due to $reason. Lives left: $lives")
        }

//        if (lives == 0) {
//            //hello whoeever is doing the terminating screen it should go here
//            shutdownGame()
//        }
    }


    fun sortReadyQueueByPriority() {
        scheduler.sortReadyQueueByPriority()
    }

    //close game
    fun shutdownGame() {
        threadPool.shutdown()
    }

}