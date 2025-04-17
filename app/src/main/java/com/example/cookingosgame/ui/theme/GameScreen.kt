package com.example.cookingosgame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookingosgame.DishProcess
import com.example.cookingosgame.GameManager
import com.example.cookingosgame.ProcessState
import com.example.cookingosgame.R
import com.example.cookingosgame.Stove
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun GameScreen(
    gameManager: GameManager,
    modifier: Modifier = Modifier
) {
    var gameTick by remember { mutableStateOf(0) }
    var frameTick by remember { mutableStateOf(0) }
    var gameEnded by remember { mutableStateOf(false)}
    var sessionId  by remember { mutableStateOf(0) }

    val frameRate = 30 // Target 30 FPS
    val frameDelay = 1000L / frameRate // ~33ms per frame


    // Start the game
    LaunchedEffect(Unit) {
        gameManager.startRestaurant()
    }

    // Run perpetual loop to update game logic and force UI recomposition
    LaunchedEffect(Unit) {
        while (true) {
            val frameStart = System.currentTimeMillis()

            frameTick++

            val frameTime = System.currentTimeMillis() - frameStart
            val sleepTime = frameDelay - frameTime
            if (sleepTime > 0) delay(sleepTime)
        }
    }

    val context = LocalContext.current

    LaunchedEffect(sessionId) {
        while (true) {
            delay(1.seconds)
            if (gameManager.lives != 0) {
                gameManager.updateGameTick(1_000L, context)
            } else {
                gameEnded = true
                gameManager.customer.stopRunning()
                gameManager.waiter.stopRunning()
                break
            }
        }
    }

    //Box container stacks children on top of one another (based on z-order)
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier
                .matchParentSize()
        )

        //parent container that arranges children vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.White)
                        .padding(vertical = 3.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Points: ${gameManager.point}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(gameManager.lives) {
                        Image(
                            painter = painterResource(id = R.drawable.heart4),
                            contentDescription = "Heart",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
            ) {
                StovesSection(gameManager)

                // Ready Queue with sorting indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color.White)
                            .padding(vertical = 2.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Order Queue (${gameManager.readyQueue.size})",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    // Add small visual indicator of current sorting
//                    Text(
//                        text = "▲ Priority", // or "▼ Priority" depending on sort
//                        color = Color.Gray,
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(start = 8.dp)
//                    )
                }
                ReadyQueueSection(gameManager)

//                // Rest remains unchanged
//                if (gameManager.waitingQueue.isNotEmpty()) {
//                    Text(
//                        text = "Waiting Queue (I/O - ${gameManager.waitingQueue.size})",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    WaitingQueueSection(gameManager)
//                }
//
//                if (gameManager.completedDishes.isNotEmpty()) {
//                    Text(
//                        text = "Completed Dishes (${gameManager.completedDishes.size})",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    CompletedDishesSection(gameManager)
//                }
//
//                if (gameManager.burntQueue.isNotEmpty()) {
//                    Text(
//                        text = "Burnt Dishes (${gameManager.burntQueue.size}/5)",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    BurntQueueSection(gameManager)
//                }
            }
        }

        if (gameEnded) {
            // semi‑transparent full‑screen overlay
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )

            // Centered “Game Over” + score + restart button
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over",
                    fontSize = 30.sp,
                    color = Color.White
                )
                Text(
                    text = "Points: ${gameManager.point}",
                    fontSize = 20.sp,
                    color = Color.White
                )

                if (gameEnded) {
                    /* overlay … */
                    Button(
                        onClick = {
                            gameManager.resetGame()
                            sessionId++
                            gameEnded = false // hide overlay
                        },
                    ) { Text("Play Again") }
                }
            }
                }
            }
        }

@Composable
private fun StovesSection(gameManager: GameManager) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 10.dp,
                horizontal = 80.dp
            ),
    ) {
        gameManager.stoves.forEach { stove ->
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StoveItem(stove, gameManager)
            }
        }
    }
}

@Composable
private fun StoveItem(stove: Stove, gameManager: GameManager) {
    val currentDish = stove.currentProcess
    val isCooking = currentDish?.state == ProcessState.RUNNING

    val statusColor = when (currentDish?.state) {
        ProcessState.RUNNING -> Color(0xFF363636)
        ProcessState.FINISHED -> Color(0xFF2FC943)
        ProcessState.BURNT -> Color(0xFFDA120F)
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .padding(top = 40.dp)
            .clickable(enabled = currentDish?.state == ProcessState.FINISHED || currentDish?.state == ProcessState.BURNT || currentDish?.state == ProcessState.STALE) {
                gameManager.managePoints(stove)
                gameManager.removeDishFromStove(stove)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            if (isCooking) {
                StoveCookingAnimation(
                    isCooking = true,
                    modifier = Modifier.size(width = 160.dp, height = 110.dp),
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.stove),
                    contentDescription = "Stove",
                    modifier = Modifier
                        .size(width = 160.dp, height = 110.dp)
                )
            }
        }

        if (currentDish != null) {
            Text(
                text = when (currentDish.state) {
                    ProcessState.RUNNING -> "COOKING..."
                    ProcessState.FINISHED -> "READY!"
                    ProcessState.BURNT -> "BURNT!"
                    else -> "Empty"
                },
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        } else {
            Text(
                text = "Empty",
                color = Color.White,
            )
        }
    }
}

@Composable
fun StoveCookingAnimation(
    isCooking: Boolean,
    modifier: Modifier = Modifier,
    frameDuration: Long = 150L
) {
    val frames = listOf(
        R.drawable.cooking_pancake_frame0,
        R.drawable.cooking_pancake_frame1,
        R.drawable.cooking_pancake_frame2,
        R.drawable.cooking_pancake_frame3,
        R.drawable.cooking_pancake_frame4,
        R.drawable.cooking_pancake_frame5
    )

    var currentFrameIndex by remember { mutableStateOf(0) }
    var directionForward by remember { mutableStateOf(true) }

    LaunchedEffect(isCooking) {
        while (isCooking) {
            delay(frameDuration)

            // Update the frame index with ping-pong logic
            if (directionForward) {
                if (currentFrameIndex < frames.lastIndex) {
                    currentFrameIndex++
                } else {
                    directionForward = false
                    currentFrameIndex--
                }
            } else {
                if (currentFrameIndex > 0) {
                    currentFrameIndex--
                } else {
                    directionForward = true
                    currentFrameIndex++
                }
            }
        }
    }

    Image(
        painter = painterResource(id = frames[currentFrameIndex]),
        contentDescription = "Cooking Stove Frame",
        modifier = modifier
    )
}


@Composable
private fun ReadyQueueSection(gameManager: GameManager) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end= 0.dp)
    ) {
        if (gameManager.readyQueue.isEmpty()) {
            Text(
                text = "No dishes waiting",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(gameManager.readyQueue) { dish ->
                    ReadyQueueItem(dish, gameManager)
                }
            }
        }
    }
}

@Composable
private fun ReadyQueueItem(dish: DishProcess, gameManager: GameManager) {
    val backgroundColor = when (dish.state) {
        ProcessState.STALE -> Color.LightGray // Yellow for stale
        else -> Color.White
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(110.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(8.dp)
            .clickable {
                // If the dish has turned stale, the user has to click to remove it
                if (dish.state == ProcessState.STALE) {
                    gameManager.point -= 50
                    gameManager.readyQueue.remove(dish)
                    gameManager.loseLife("Stale")
                } else

                gameManager.stoves.firstOrNull { it.isFree() }?.let { stove ->
                    gameManager.assignDishToStove(dish, stove)
                    gameManager.readyQueue.remove(dish)
                }
            },
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colorForPriority(dish.priority))
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dish.name,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cook time: ${dish.burstTime / 1000}s",
                fontSize = 12.sp
            )
            if (dish.state == ProcessState.STALE) {
                Text(
                    text = "STALE!",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "Priority: ${dish.priority}",
            fontSize = 12.sp
        )
    }
}

//@Composable
//private fun WaitingQueueSection(gameManager: GameManager) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(80.dp)
//            .clip(RoundedCornerShape(8.dp))
//            .background(Color(0xFFFFF3E0))
//            .border(1.dp, Color(0xFFFFA000), RoundedCornerShape(8.dp))
//            .padding(8.dp)
//    ) {
//        if (gameManager.waitingQueue.isEmpty()) {
//            Text(
//                text = "No dishes waiting for I/O",
//                modifier = Modifier.align(Alignment.Center),
//                color = Color.Gray
//            )
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                items(gameManager.waitingQueue) { dish ->
//                    WaitingQueueItem(dish)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun WaitingQueueItem(dish: DishProcess) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(4.dp))
//            .background(Color.White)
//            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
//            .padding(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier
//                .size(24.dp)
//                .clip(CircleShape)
//                .background(Color(0xFFFFA000))
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = dish.name,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(modifier = Modifier.weight(1f))
//        Text(
//            text = "I/O: ${dish.ioWaitTime / 1000}s",
//            fontSize = 12.sp
//        )
//    }
//}

//@Composable
//private fun CompletedDishesSection(gameManager: GameManager) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(100.dp)
//            .clip(RoundedCornerShape(8.dp))
//            .background(Color(0xFFE8F5E9))
//            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
//            .padding(8.dp)
//    ) {
//        if (gameManager.completedDishes.isEmpty()) {
//            Text(
//                text = "No completed dishes yet",
//                modifier = Modifier.align(Alignment.Center),
//                color = Color.Gray
//            )
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                items(gameManager.completedDishes) { dish ->
//                    CompletedDishItem(dish)
//                }
//            }
//        }
//    }
//}

//@Composable
//private fun CompletedDishItem(dish: DishProcess) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(4.dp))
//            .background(Color.White)
//            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
//            .padding(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier
//                .size(24.dp)
//                .clip(CircleShape)
//                .background(Color(0xFF4CAF50))
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = dish.name,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF2E7D32)
//        )
//        Spacer(modifier = Modifier.weight(1f))
//        Text(
//            text = "Completed!",
//            fontSize = 12.sp,
//            color = Color(0xFF2E7D32)
//        )
//    }
//}

@Composable
private fun DishInfo(dish: DishProcess) {
    val statusColor = when (dish.state) {
        ProcessState.RUNNING -> Color(0xFF363636)
        ProcessState.FINISHED -> Color(0xFF2FC943)
        ProcessState.BURNT -> Color(0xFFDA120F)
        else -> Color.Gray
    }

    Text(
        text = when (dish.state) {
            ProcessState.RUNNING -> "COOKING..."
            ProcessState.FINISHED -> "READY!"
            ProcessState.BURNT -> "BURNT!"
            else -> ""
        },
        fontWeight = FontWeight.Bold,
        color = statusColor
    )
}

private fun colorForPriority(priority: Int): Color {
    return when (priority) {
        1 -> Color(0xFFEF5350) // Red
        2 -> Color(0xFFFFA726) // Orange
        3 -> Color(0xFFFFEE58) // Yellow
        4 -> Color(0xFF66BB6A) // Green
        5 -> Color(0xFF42A5F5) // Blue
        else -> Color.Gray
    }
}
//
//@Composable
//private fun BurntQueueSection(gameManager: GameManager) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(80.dp)
//            .clip(RoundedCornerShape(8.dp))
//            .background(Color(0xFFFFF3E0))
//            .border(1.dp, Color(0xFFFFA000), RoundedCornerShape(8.dp))
//            .padding(8.dp)
//    ) {
//        if (gameManager.burntQueue.isEmpty()) {
//            Text(
//                text = "No burnt dishes",
//                modifier = Modifier.align(Alignment.Center),
//                color = Color.Gray
//            )
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                items(gameManager.burntQueue) { dish ->
//                    BurntItem(dish)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun BurntItem(dish: DishProcess) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(4.dp))
//            .background(Color.White)
//            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
//            .padding(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier
//                .size(24.dp)
//                .clip(CircleShape)
//                .background(Color(0xFFFFA000))
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = dish.name,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(modifier = Modifier.weight(1f))
//        Text(
//            text = "I/O: ${dish.ioWaitTime / 1000}s",
//            fontSize = 12.sp
//        )
//    }
//}
