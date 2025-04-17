package com.example.cookingosgame

//manage time-based process state transition
//does not assign dish to stove as player does that
// simulates i/o completion, starvation and queue logic

class Scheduler(
    private val readyQueue: MutableList<DishProcess>, // processes ready to be cooked
    private val stoves: List<Stove> // processes cooking on the stoves
) {

    fun update(timeDelta: Long) {
        updateReadyProcesses(timeDelta)
        updateStoveProcesses(timeDelta)
    }

    // Update all processes in ready queue
    private fun updateReadyProcesses(timeDelta: Long) {
        for (process in readyQueue) {
            process.waitingTime += timeDelta

            // If the process has been waiting too long, the dish becomes stale
            if (process.waitingTime >= process.maxWaitTime) {
                process.state = ProcessState.STALE
            }
        }
    }

    // Update all processes on all stoves
    private fun updateStoveProcesses(timeDelta: Long) {

        for (stove in stoves) {
            val dish = stove.currentProcess
            if (dish != null && dish.state == ProcessState.FINISHED) {
                dish.timeSinceFinished += timeDelta

            }
        }
    }

    fun sortReadyQueueByPriority() {
        readyQueue.sortBy { it.priority }
    }
}
