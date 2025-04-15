package com.example.cookingosgame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

    val frameRate = 30 // Target 30 FPS
    val frameDelay = 1000L / frameRate // ~33ms per frame

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

    LaunchedEffect(Unit) {
        while(true) {
            delay(1.seconds)
            gameManager.updateGameTick(1000L) // Pass delta time
            gameTick++ // Trigger recomposition (can also just use rememberUpdatedState if needed)
        }
    }

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

        Text(
            text = "  Points: ${gameManager.point}",
            style = MaterialTheme.typography.titleMedium,
        )

        Column(

            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {
//        // Game title
//        Text(
//            text = "Cooking OS Simulator",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.fillMaxWidth(),
//            textAlign = TextAlign.Center,
//            fontWeight = FontWeight.Bold
//        )

//        // Add sorting controls right below the title
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 8.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            // Priority sorting button (lower number = higher priority)
//            Button(
//                onClick = { gameManager.sortReadyQueueByPriority() },
//                modifier = Modifier.weight(1f).padding(end = 4.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF4CAF50) // Green
//                )
//            ) {
//                Text("Sort by Priority", fontSize = 12.sp)
//            }
//        }

            // Stoves section (unchanged)
//            Text(
//                text = "Stoves (CPU Cores)",
//                style = MaterialTheme.typography.titleMedium
//            )


            // Top Row Padding (Potentially add points count & Pause button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
            ) {
                Text(
                    text=frameTick.toString()
                )
            }

            StovesSection(gameManager)

            // Ready Queue with sorting indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Order Queue (${gameManager.readyQueue.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                // Add small visual indicator of current sorting
                Text(
                    text = "▲ Priority", // or "▼ Priority" depending on sort
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            ReadyQueueSection(gameManager)

            // Rest remains unchanged
            if (gameManager.waitingQueue.isNotEmpty()) {
                Text(
                    text = "Waiting Queue (I/O - ${gameManager.waitingQueue.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                WaitingQueueSection(gameManager)
            }

            if (gameManager.completedDishes.isNotEmpty()) {
                Text(
                    text = "Completed Dishes (${gameManager.completedDishes.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                CompletedDishesSection(gameManager)
            }

            if (gameManager.burntQueue.isNotEmpty()) {
                Text(
                    text = "Burnt Dishes (${gameManager.burntQueue.size}/5)",
                    style = MaterialTheme.typography.titleMedium
                )
                BurntQueueSection(gameManager)
            }
        }
    }
}

@Composable
private fun StovesSection(gameManager: GameManager) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
//    val backgroundColor = when {
//        currentDish == null -> Color.LightGray
//        currentDish.state == ProcessState.FINISHED -> Color(0xFFFFCC80) // Orange for finished
//        currentDish.state == ProcessState.BURNT -> Color(0xFFEF9A9A) // Red for burnt
//        else -> Color(0xFFC8E6C9) // Green for cooking
//    }

    val StoveImage =

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable(enabled = currentDish?.state == ProcessState.FINISHED || currentDish?.state == ProcessState.BURNT || currentDish?.state == ProcessState.STALE) {
                gameManager.managePoints(stove)
                gameManager.removeDishFromStove(stove)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
//        Text(
//            text = "Stove ${stove.id + 1}",
//            fontWeight = FontWeight.Bold
//        )

        Box(
            modifier = Modifier
                .size(128.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.stove),
                contentDescription = "Stove",
                modifier = Modifier
                    .matchParentSize()
            )
        }


        if (currentDish != null) {
            DishInfo(currentDish)
            if (currentDish.state == ProcessState.FINISHED) {
                Text(
                    text = "Tap to serve",
                    color = Color.Blue,
                    fontSize = 12.sp
                )
            } else if (currentDish.state == ProcessState.BURNT) {
                Text(
                    text = "BURNT!",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text("Empty")
        }
    }
}

@Composable
private fun ReadyQueueSection(gameManager: GameManager) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE3F2FD))
            .border(1.dp, Color.Blue, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (gameManager.readyQueue.isEmpty()) {
            Text(
                text = "No dishes waiting",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
        ProcessState.STALE -> Color(0xFFFFF9C4) // Yellow for stale
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(8.dp)
            .clickable {
                if (dish.state == ProcessState.STALE) {
                    gameManager.point -= 50
                    gameManager.readyQueue.remove(dish)
                } else
                gameManager.stoves.firstOrNull { it.isFree() }?.let { stove ->
                    gameManager.assignDishToStove(dish, stove)
                    gameManager.readyQueue.remove(dish)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
private fun WaitingQueueSection(gameManager: GameManager) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF3E0))
            .border(1.dp, Color(0xFFFFA000), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (gameManager.waitingQueue.isEmpty()) {
            Text(
                text = "No dishes waiting for I/O",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(gameManager.waitingQueue) { dish ->
                    WaitingQueueItem(dish)
                }
            }
        }
    }
}

@Composable
private fun WaitingQueueItem(dish: DishProcess) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFA000))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dish.name,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "I/O: ${dish.ioWaitTime / 1000}s",
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CompletedDishesSection(gameManager: GameManager) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8F5E9))
            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (gameManager.completedDishes.isEmpty()) {
            Text(
                text = "No completed dishes yet",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(gameManager.completedDishes) { dish ->
                    CompletedDishItem(dish)
                }
            }
        }
    }
}

@Composable
private fun CompletedDishItem(dish: DishProcess) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dish.name,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Completed!",
            fontSize = 12.sp,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun DishInfo(dish: DishProcess) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dish.name,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = when (dish.state) {
                ProcessState.RUNNING -> "Cooking..."
                ProcessState.FINISHED -> "Done!"
                ProcessState.BURNT -> "BURNT!"
                else -> ""
            },
            fontSize = 12.sp
        )
    }
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

@Composable
private fun BurntQueueSection(gameManager: GameManager) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF3E0))
            .border(1.dp, Color(0xFFFFA000), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (gameManager.burntQueue.isEmpty()) {
            Text(
                text = "No burnt dishes",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(gameManager.burntQueue) { dish ->
                    BurntItem(dish)
                }
            }
        }
    }
}

@Composable
private fun BurntItem(dish: DishProcess) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFA000))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dish.name,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "I/O: ${dish.ioWaitTime / 1000}s",
            fontSize = 12.sp
        )
    }
}
