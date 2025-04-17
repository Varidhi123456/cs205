package com.example.cookingosgame

// This class will act as a buffer to the orders before they are sent to the kitchen
class OrderList<DishProcess>(private val capacity: Int, private val gameManager: GameManager) {
    private val queue = mutableListOf<DishProcess>()
    private val lock = Object() // Lock object for synchronization

    // This function will be called by the producer thread to add an item to the buffer
    fun produce(item: DishProcess) {
        synchronized(lock) {
            while (queue.size == capacity) {
                println("Buffer full, producer waiting...")
                lock.wait()
            }
            queue.add(item)
            println("Produced: $item | Buffer: $queue")
            lock.notifyAll()
        }
    }

    // This function will be called by the consumer thread to remove an item from the buffer
    fun consume(): DishProcess {
        synchronized(lock) {
            while (queue.isEmpty() || gameManager.readyQueue.size >= 10) {
                println("Buffer empty, consumer waiting...")
                lock.wait()
            }
            val dish = queue.removeAt(0)
            println("Consumed: $dish | Buffer: $queue")
            lock.notifyAll()
            return dish
        }
    }

    fun clear() {
        synchronized(lock) {
            queue.clear()
        }
    }
}

