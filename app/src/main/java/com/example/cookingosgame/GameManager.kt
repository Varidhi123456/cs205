package com.example.cookingosgame
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
    val completedDishes = mutableStateListOf<DishProcess>() //this is for when the dish are cooked completely
    var point = 0// track points
    private val allDishes = mutableListOf<DishProcess>()
    private var dishIdCounter = 1
    private var ticksPassed = 0



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
            burstTime = (3000L..7000L).random(),             // Simulate 3–7 sec cook
            ioWaitTime = listOf(0L, 1000L, 2000L).random(),   // Random I/O wait
            priority = (1..5).random()
        )
        addNewDish(dish)
        allDishes.add(dish)
        return dish
    }

    private fun generateRandomDishName(): String {
        val names = listOf("Steak", "Soup", "Burger", "Butter Chicken", "Curry", "Fish", "Pasta", "Omelette")
        return names.random()
    }

    fun updateGameTick(timeDelta: Long) {
        scheduler.update(timeDelta)
        ticksPassed += 1

        // spawn new dishes more frequently as game progresses (optional)
        val spawnInterval = 10

        if (ticksPassed % spawnInterval == 0) {
            println("New order incoming!") //debug
            generateNewDish()
        }

        // Check for burnt dishes
        for (stove in stoves) {
            val dish = stove.currentProcess
            if (dish != null && dish.state == ProcessState.FINISHED) {
                dish.timeSinceFinished += timeDelta

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
            println("Dish '${finishedDish.name}' burnt!")
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
        if (finishedDish?.state == ProcessState.STALE) {
            point = point - 50
        }

    }

    fun sortReadyQueueByPriority() {
        scheduler.sortReadyQueueByPriority()
    }


    //close game
    fun shutdownGame() {
        threadPool.shutdown()
    }

}