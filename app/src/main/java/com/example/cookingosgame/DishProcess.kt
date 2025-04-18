package com.example.cookingosgame


enum class ProcessState {
    READY, RUNNING, STALE, FINISHED, BURNT
}
data class DishProcess(
//    val id: Int,
    val name: String,
    val burstTime: Long,        // Total time to cook
    var dishPriority: Int,
    var state: ProcessState = ProcessState.READY,
    var waitingTime: Long = 0L, // Total time in READY
    var maxWaitTime: Long, // Time before it turns stale
    var startTimeMillis: Long = 0L, // Time dish started cooking
    var timeSinceFinished: Long = 0L, // Time dish has been waiting after finish cooking
    var notified: Boolean = false // Keep track of if the user has already been notified the dish is done
)