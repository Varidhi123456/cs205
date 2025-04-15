package com.example.cookingosgame.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookingosgame.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp


@Composable
fun LandingPage(
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.landingpage),
            contentDescription = "Background",
            contentScale = ContentScale.Crop, // Ensures the image covers the screen
            modifier = Modifier.fillMaxSize()
        )

        // Overlay content on top of the background image
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "Welcome to Cooking OS",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = Color.White,
                modifier = Modifier.offset(y = (-85).dp)

            )


            // Optional: Logo image (if needed)
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Game Logo",
                modifier = Modifier.offset(y = (-200).dp)
            )

            Text(
                text = "Ready for an immersive cooking challenge?",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-50).dp),
                fontSize = 18.5.sp,
                color = androidx.compose.ui.graphics.Color.White
            )

            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(200.dp)  // Set a fixed width of 200.dp
                    .height(60.dp), // Set a fixed height of 60.dp
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,  // Sets the button's background to red
                    contentColor = Color.White   // Sets the text color to white
                )
            ) {
                Text(text = "Start Cooking!",
                    fontSize = 20.sp)
            }
        }
    }
}
