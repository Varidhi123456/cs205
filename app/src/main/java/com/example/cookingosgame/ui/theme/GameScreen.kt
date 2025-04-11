package com.example.cookingosgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookingosgame.*
import kotlinx.coroutines.delay

@Composable
fun GameScreen(gameManager: GameManager, modifier: Modifier = Modifier) {
    val stoves = gameManager.stoves
    val readyQueue by remember { mutableStateOf(gameManager.readyQueue) }
    val completedDishes by remember { mutableStateOf(gameManager.completedDishes) }

    // Trigger game tick every 1 second
    LaunchedEffect(Unit) {
        while (true) {
            gameManager.updateGameTick(1000L)
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("OS Cooking Simulator", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        // Show stoves
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stoves.forEach { stove ->
                StoveView(stove = stove, onDishRemoved = {
                    gameManager.removeDishFromStove(stove)
                })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Show ready queue
        Text("Ready Queue", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            readyQueue.forEach { dish ->
                DishCard(dish = dish, onDragEnd = { stove ->
                    gameManager.assignDishToStove(dish, stove)
                })
            }
        }
    }
}

@Composable
fun StoveView(stove: Stove, onDishRemoved: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        val dish = stove.currentProcess
        if (dish != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(dish.name)
                Text(dish.state.name, fontSize = 12.sp)
                if (dish.state == ProcessState.FINISHED || dish.state == ProcessState.BURNT) {
                    Button(onClick = onDishRemoved) {
                        Text("Remove")
                    }
                }
            }
        } else {
            Text("Empty")
        }
    }
}

@Composable
fun DishCard(dish: DishProcess, onDragEnd: (Stove) -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(80.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .shadow(2.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // TODO: Replace with real stove drop detection logic
                        println("Dropped ${dish.name}")
                    },
                    onDrag = { _, dragAmount ->
                        offsetX += dragAmount.x
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(dish.name, fontSize = 14.sp)
    }
}
