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
    val burntQueue = mutableStateListOf<DishProcess>()
    private val burnThresholdMs = 10000L // Time before dish burns
    val completedDishes = mutableStateListOf<DishProcess>() // List of completed dishes
    var point = 0 // Track points
    var lives = 5 // Initial lives
    private var ticksPassed = 0

    // Shared thread pool for cooking simulation so lets say 4 stove i have 4 worker/chefs
    private val threadPool: ExecutorService = Executors.newFixedThreadPool(4)
    // Simulate the 4 CPU cores (stoves)
    val stoves = List(4){id->Stove(id, threadPool)}
    // Scheduler tracks timing logic
    private val scheduler = Scheduler(readyQueue, stoves)

    private val orderList: OrderList<DishProcess> = OrderList(10, this)

    //create customer and waiter instance
    lateinit var customer: Customer
    lateinit var waiter: Waiter

    // Reboot everything back to its initial values.
    fun resetGame() {
        point = 0
        lives = 5
        orderList.clear()
        readyQueue.clear()
        completedDishes.clear()
        burntQueue.clear()
        ticksPassed = 0
        stoves.forEach { it.manuallyRemoveProcess() }
        startRestaurant()
    }

    // Start the restaurant logic
    fun startRestaurant() {
        customer = Customer(orderList)
        waiter = Waiter(orderList, this)

        customer.start()
        waiter.start()
    }

    fun updateGameTick(timeDelta: Long, context: Context) {
        scheduler.update(timeDelta)
        ticksPassed += 1

        // Check the dishes on the stove
        for (stove in stoves) {
            val dish = stove.currentProcess
            if (dish != null && dish.state == ProcessState.FINISHED) {

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

    // Placing dish on stove
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