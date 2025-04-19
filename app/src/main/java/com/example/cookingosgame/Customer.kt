package com.example.cookingosgame

// This class will act as the producer, making orders for the kitchen to make
class Customer(
    private val orderList: OrderList<DishProcess>,
) : Thread() {
    private val dishNames = listOf("Steak", "Bacon & Eggs", "Grilled Fish", "Pancakes")
    private var isRunning = true

    override fun run() {
        while (isRunning) {
            try {
                // Simulate the time it takes to decide on an order (4-6 seconds)
                val orderingTime = (4000L..6000L).random()
                sleep(orderingTime)
                val dish = orderDish()
                orderList.produce(dish) // Add the order to the queue
            } catch (e: InterruptedException) {
                // Thread was interrupted, time to shut down
                println("Customer thread interrupted.")
                isRunning = false
            }
        }
    }

    // Order a random dish from the menu
    private fun orderDish(): DishProcess {
        val dishPriority = (1..5).random()
        val waitTime = generateWaitTime(dishPriority)
        val dish = DishProcess(
            name = dishNames.random(),
            burstTime = (3000L..7000L).random(),  // Simulate 3–7 sec cook
            dishPriority = dishPriority,
            maxWaitTime = waitTime
        )
        return dish
    }

    // Generate a random wait time based on the dish's priority
    private fun generateWaitTime(priority: Int): Long {
        return when (priority) {
            1 -> (5000L..6000L).random()
            2 -> (6000L..7000L).random()
            3 -> (7000L..8000L).random()
            4 -> (8000L..9000L).random()
            5 -> (9000L..10000L).random()
            else -> 0
        }
    }

    fun stopRunning() {
        isRunning = false
    }
}