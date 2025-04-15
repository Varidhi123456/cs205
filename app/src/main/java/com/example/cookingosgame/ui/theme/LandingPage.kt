package com.example.cookingosgame.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookingosgame.R

@Composable
fun LandingPage(
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Root container with a background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212)), // dark background color
        contentAlignment = Alignment.Center
    ) {
        // Layout content in a vertical column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Title text
            Text(
                text = "Welcome to Cooking OS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Game Logo",
                modifier = Modifier.size(120.dp)
            )

            // description text
            Text(
                text = "Get ready for an immersive cooking challenge!",
                fontSize = 16.sp,
                color = Color.LightGray
            )

            // Start Game Button
            Button(
                onClick = onStartGame
            ) {
                Text(text = "Start Game")
            }
        }
    }
}
