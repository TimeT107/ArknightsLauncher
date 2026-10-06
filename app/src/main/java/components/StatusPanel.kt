package com.example.arklauncher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arklauncher.data.DayMode
import com.example.arklauncher.data.LauncherStatus

@Composable
fun StatusPanel(
    status: LauncherStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.width(200.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左线
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xCCFFFFFF))
        )

        // 中间数据
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (status.dayMode == DayMode.DAY) "☀" else "☾",
                fontSize = 13.sp,
                color = Color(0xFFFFD54F)
            )
            Text(
                text = status.date,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = status.time,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        // 右线
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xCCFFFFFF))
        )
    }
}