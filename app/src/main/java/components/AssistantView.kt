package com.example.arklauncher.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.arklauncher.R

@Composable
fun AssistantView(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    contentScale: ContentScale = ContentScale.Fit
) {
    Image(
        painter = painterResource(id = R.drawable.assistant),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier.alpha(alpha)
    )
}