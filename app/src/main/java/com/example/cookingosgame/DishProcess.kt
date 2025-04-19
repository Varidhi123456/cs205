package com.example.cookingosgame


enum class ProcessState {
    READY, RUNNING, STALE, FINISHED, BURNT
}
data class DishProcess(
    val name: String,
    val burstTime: Long, // Total time taken to cook
    val dishPriority: Int,
    var state: ProcessState = ProcessState.READY,
    var waitingTime: Long = 0L, // Total time in READY state
    var maxWaitTime: Long, // Time before ingredients prepared for the dish turn stale
    var startTimeMillis: Long = 0L, // Time dish started cooking
    var timeSinceFinished: Long = 0L, // Time dish has been waiting after finish cooking
    var notified: Boolean = false // Keep track of if the user has already been notified the dish is done
)