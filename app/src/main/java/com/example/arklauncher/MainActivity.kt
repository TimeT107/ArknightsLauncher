package com.example.arklauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.arklauncher.data.LauncherState
import com.example.arklauncher.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LauncherState.init(applicationContext)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            HomeScreen()
        }
    }
}