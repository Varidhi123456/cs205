package com.example.cookingosgame

// This class will act as the consumer, delivering orders from the customer to the kitchen
class Waiter(
    private val orderList: OrderList<DishProcess>,
    private val gameManager: GameManager
) : Thread() {
    private var isRunning = true

    override fun run() {
        while (isRunning) {
            try {
                val order = orderList.consume() // Take an order from the queue (blocks if empty)
                gameManager.readyQueue.add(order) // Add the order to the ready queue
//                println("Waiter delivered order: ${order.name}")
            } catch (e: InterruptedException) {
                // Thread was interrupted, time to shut down
                println("Waiter thread interrupted.")
                isRunning = false
            }
        }
//        println("Waiter thread exiting.")
    }

    fun stopRunning() {
        isRunning = false
    }
    fun startRunning() {
        isRunning = true
    }
}