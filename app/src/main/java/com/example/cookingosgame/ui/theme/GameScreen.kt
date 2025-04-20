package com.example.cookingosgame.ui.theme

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
                StovesSection(gameManager, frameTick)

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
                }
                ReadyQueueSection(
                    gameManager
                )
            }
        }

        if (gameEnded) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )

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
                    Button(
                        onClick = {
                            gameManager.resetGame()
                            sessionId++
                            gameEnded = false
                        },
                    ) { Text("Play Again") }
                }
            }
        }
    }
}

@Composable
private fun StovesSection(gameManager: GameManager, frameTick: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 10.dp,
                horizontal = 80.dp
            ),
    ) {
        gameManager.stoves.forEach { stove ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // The stove visual
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    StoveItem(stove, gameManager)
                }

                // Progress bar under the stove
                if (!stove.isFree() && stove.currentProcess!!.state != ProcessState.BURNT) {
                    stove.currentProcess?.let {
                        CookingProgressBar(
                            durationMillis = it.burstTime, // Total cooking duration
                            startTimeMillis = it.startTimeMillis, // Start of cooking time
                            frameTick = frameTick,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .fillMaxWidth(0.8f)
                                .height(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CookingProgressBar(
    durationMillis: Long,
    startTimeMillis: Long,
    frameTick: Int,
    modifier: Modifier = Modifier
) {
    // Use frameTick to trigger recomposition
    val temp = frameTick

    val currentTime = System.currentTimeMillis()
    val elapsed = currentTime - startTimeMillis
    val progress = (elapsed.toFloat() / durationMillis).coerceIn(0f, 1f)

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray),
        color = Color(0xFF4CAF50),
        trackColor = Color.DarkGray,
    )
}

@Composable
private fun StoveItem(stove: Stove, gameManager: GameManager) {
    val currentDish = stove.currentProcess
    val isCooking = currentDish?.state == ProcessState.RUNNING
    val isReady = currentDish?.state == ProcessState.FINISHED
    val isBurnt = currentDish?.state == ProcessState.BURNT

    val statusColor = when (currentDish?.state) {
        ProcessState.RUNNING -> Color(0xFF363636)
        ProcessState.FINISHED -> Color(0xFF2FC943)
        ProcessState.BURNT -> Color(0xFFDA120F)
        else -> Color.Gray
    }

    val cookedImage = when (currentDish?.name) {
        "Bacon & Eggs" -> R.drawable.cooked_eggs_and_bacon
        "Grilled Fish" -> R.drawable.cooked_fish
        "Pancakes" -> R.drawable.cooked_pancake
        else -> R.drawable.cooked_steak
    }

    val burntImage = when (currentDish?.name) {
        "Bacon & Eggs" -> R.drawable.burnt_eggs_and_bacon
        "Grilled Fish" -> R.drawable.burnt_fish
        "Pancakes" -> R.drawable.burnt_pancake
        else -> R.drawable.burnt_steak
    }

    Column(
        modifier = Modifier
            .padding(top = 40.dp)
            .clickable(enabled = currentDish?.state == ProcessState.FINISHED || currentDish?.state == ProcessState.BURNT) {
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
            if (isCooking && currentDish != null) {
                StoveCookingAnimation(
                    isCooking = true,
                    dishName = currentDish.name,
                    modifier = Modifier.size(width = 160.dp, height = 110.dp),
                )
            } else if (isReady) {
                Image(
                    painter = painterResource(id = cookedImage),
                    contentDescription = "Stove",
                    modifier = Modifier
                        .size(width = 160.dp, height = 110.dp)
                )
            } else if (isBurnt) {
                Image(
                    painter = painterResource(id = burntImage),
                    contentDescription = "Stove",
                    modifier = Modifier
                        .size(width = 160.dp, height = 110.dp)
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
    dishName: String,
    modifier: Modifier = Modifier,
    frameDuration: Long = 150L
) {
    val frames = if (dishName == "Bacon & Eggs") {
        listOf(
            R.drawable.cooking_eggs_and_bacon_frame0,
            R.drawable.cooking_eggs_and_bacon_frame1,
            R.drawable.cooking_eggs_and_bacon_frame2,
            R.drawable.cooking_eggs_and_bacon_frame3,
            R.drawable.cooking_eggs_and_bacon_frame4,
            R.drawable.cooking_eggs_and_bacon_frame5
        )
    } else if (dishName == "Grilled Fish") {
        listOf(
            R.drawable.cooking_fish_frame0,
            R.drawable.cooking_fish_frame1,
            R.drawable.cooking_fish_frame2,
            R.drawable.cooking_fish_frame3,
            R.drawable.cooking_fish_frame4,
            R.drawable.cooking_fish_frame5
        )
    } else if (dishName == "Pancakes") {
        listOf(
            R.drawable.cooking_pancake_frame0,
            R.drawable.cooking_pancake_frame1,
            R.drawable.cooking_pancake_frame2,
            R.drawable.cooking_pancake_frame3,
            R.drawable.cooking_pancake_frame4,
            R.drawable.cooking_pancake_frame5
        )
    } else {
        listOf(
            R.drawable.cooking_steak_frame0,
            R.drawable.cooking_steak_frame1,
            R.drawable.cooking_steak_frame2,
            R.drawable.cooking_steak_frame3,
            R.drawable.cooking_steak_frame4,
            R.drawable.cooking_steak_frame5
        )
    }

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
        ProcessState.STALE -> Color(0xFFF18181)
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
            }
    ) {
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

        if (dish.state != ProcessState.STALE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorForPriority(dish.dishPriority))
                )
                Text(
                    text = "Priority: ${dish.dishPriority}",
                    fontSize = 12.sp
                )
            }
        }
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