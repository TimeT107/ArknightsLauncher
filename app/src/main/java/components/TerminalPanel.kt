package com.example.arklauncher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TerminalPanel() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TerminalButton(icon = "◇", title = "TERMINAL", name = "终端")
        TerminalButton(icon = "▣", title = "BASE", name = "基建")
        TerminalButton(icon = "□", title = "WAREHOUSE", name = "仓库")
        TerminalButton(icon = "✦", title = "RECRUIT", name = "招募")
    }
}

@Composable
fun TerminalButton(
    icon: String,
    title: String,
    name: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xAA101010))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 24.sp, color = Color.White)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 12.sp, color = Color.LightGray)
                Text(text = name, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}