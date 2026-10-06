package com.example.arklauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arklauncher.data.LauncherState

@Composable
fun RightPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        StatusPanel(status = LauncherState.status.value)
        Spacer(Modifier.height(30.dp))
        TerminalPanel()
    }
}