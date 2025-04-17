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
                Thread.sleep(orderingTime)
                val dish = orderDish()
                orderList.produce(dish) // Add the order to the queue
            } catch (e: InterruptedException) {
                // Thread was interrupted, time to shut down
                println("Customer thread interrupted.")
                isRunning = false
            }
        }
    }

    private fun orderDish(): DishProcess {
        val dish = DishProcess(
            name = dishNames.random(),
            burstTime = (3000L..7000L).random(),  // Simulate 3–7 sec cook
            priority = (1..5).random()
        )
        return dish
    }

    fun stopRunning() {
        isRunning = false
    }
}