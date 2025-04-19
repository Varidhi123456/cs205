package com.example.cookingosgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.cookingosgame.ui.theme.GameScreen
import com.example.cookingosgame.ui.theme.CookingOSGameTheme
import androidx.compose.runtime.setValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        // Allows the app content to extend into system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide system bars (status bar + navigation bar) in immersive mode
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            CookingOSGameTheme {
                // Introduce a boolean state to track whether to show the landing screen.
                var showLanding by remember { androidx.compose.runtime.mutableStateOf(true) }

                if (showLanding) {
                    // Show the landing page, and when the user clicks “Start Game”
                    // it sets the flag to false.
                    com.example.cookingosgame.ui.theme.LandingPage(onStartGame = { showLanding = false })
                } else {
                    // Once the landing page flag is set to false, initialize the game manager
                    // and show the game screen within your existing Scaffold.
                    val gameManager = remember { GameManager() }
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        GameScreen(
                            gameManager = gameManager,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
