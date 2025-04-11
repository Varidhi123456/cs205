package com.example.cookingosgame


enum class ProcessState {
    READY, RUNNING, WAITING, STALE, FINISHED, BURNT
}
data class DishProcess(
    val id: Int,
    val name: String,
    val burstTime: Long,        // total time to cook
    val ioWaitTime: Long = 0L,  // time spent in I/O (ingredient prep)
    val priority: Int = 1,
    var state: ProcessState = ProcessState.READY,
    var elapsedTime: Long = 0L, // how much time has passed on the stove
    var waitingTime: Long = 0L, // how long in READY/WAITING
    val maxWaitTime: Long = 10000L, // when it turns stale
    var timeSinceFinished: Long = 0L // Time dish has been waiting after cooking
)