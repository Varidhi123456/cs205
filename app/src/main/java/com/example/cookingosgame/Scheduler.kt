package com.example.cookingosgame

//manage time-based process state traansition
//does not assign dish to stove as player does that
// simulates i/o completion, starvation and queue logic

class Scheduler(
    private val readyQueue: MutableList<DishProcess>, //process ready to be cooked
    private val waitingQueue: MutableList<DishProcess> // process waiting for I/O
) {
    // this should be called every game tick (e.g., every 16ms or 1s) but im not too sure about this.
    // your can change it if your wan
    fun update(timeDelta: Long) {
        updateWaitingProcesses(timeDelta)
        updateReadyProcesses(timeDelta)
    }
    // update all process in the waiting queue
    //increase wait itime and check
    //If i/o time is done move to ready
    //if waited too long marked them as stale
    private fun updateWaitingProcesses(timeDelta: Long) {
        val toReady = mutableListOf<DishProcess>()

        for (process in waitingQueue) {
            process.waitingTime += timeDelta

            // simulate I/O wait completed and ready to be dragged to the stove
            if (process.waitingTime >= process.ioWaitTime) {
                process.state = ProcessState.READY
                toReady.add(process)
            } else if (process.waitingTime >= process.maxWaitTime) {
                process.state = ProcessState.STALE
            }
        }

        // move dishes from waiting to ready
        waitingQueue.removeAll(toReady)
        readyQueue.addAll(toReady)
    }
    //update all process in ready queue
    //increase waiting time and check for starvation as well?
    private fun updateReadyProcesses(timeDelta: Long) {
        for (process in readyQueue) {
            process.waitingTime += timeDelta

            if (process.waitingTime >= process.maxWaitTime) {
                process.state = ProcessState.STALE
            }
        }
    }

    //not sure if we want this feature
//    fun sortReadyQueueBySJF() {
//        readyQueue.sortBy { it.burstTime }
//    }
//
//    fun sortReadyQueueByPriority() {
//        readyQueue.sortBy { it.priority }
//    }
}
