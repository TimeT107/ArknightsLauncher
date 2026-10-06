package com.example.arklauncher.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.arklauncher.R
import com.example.arklauncher.data.DayMode

@Composable
fun BackgroundLayer(mode: DayMode) {
    val res = when (mode) {
        DayMode.DAY -> R.drawable.bg_day
        DayMode.NIGHT -> R.drawable.bg_night
    }
    Image(
        painter = painterResource(id = res),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}