package com.example.arklauncher.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arklauncher.data.QuickToggles
import com.example.arklauncher.data.SystemStatus
import com.example.arklauncher.data.SystemStatusProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape

private val DashTextGray = Color(0x99FFFFFF)
private val ToggleOnBg = Color(0xFF1FB4F0)
private val ToggleOffBg = Color(0xFF2A2A2A)
private val BarTrack = Color(0x33FFFFFF)
private val BarBlue = Color(0xFF4FC3F7)
private val BarGreen = Color(0xFF66BB6A)
private val BarYellow = Color(0xFFFFCA28)
private val BarRed = Color(0xFFEF5350)

@Composable
fun DashboardPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(SystemStatus()) }
    var wifiOn by remember { mutableStateOf(false) }
    var btOn by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }
    var silentOn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            status = withContext(Dispatchers.IO) {
                SystemStatusProvider.getStatus(context)
            }
            wifiOn = QuickToggles.isWifiOn(context)
            btOn = QuickToggles.isBluetoothOn(context)
            torchOn = QuickToggles.isTorchOn(context)
            silentOn = QuickToggles.isSilentOn(context)
            delay(3000L)
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左：4 个开关
        Column(
            Modifier
                .weight(0.55f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ToggleTile("WiFi", wifiOn, Modifier.weight(1f)) {
                    QuickToggles.toggleWifi(context)
                    wifiOn = QuickToggles.isWifiOn(context)
                }
                ToggleTile("蓝牙", btOn, Modifier.weight(1f)) {
                    QuickToggles.toggleBluetooth(context)
                }
            }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ToggleTile("手电", torchOn, Modifier.weight(1f)) {
                    QuickToggles.toggleTorch(context)
                    torchOn = QuickToggles.isTorchOn(context)
                }
                ToggleTile("静音", silentOn, Modifier.weight(1f)) {
                    QuickToggles.toggleSilent(context)
                    silentOn = QuickToggles.isSilentOn(context)
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // 右：4 个状态条
        Column(
            Modifier
                .weight(0.45f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            StatusBar("CPU", status.cpuPercent, Modifier.weight(1f))
            StatusBar("MEM", status.memPercent, Modifier.weight(1f))
            StatusBar("DSK", status.diskPercent, Modifier.weight(1f))
            StatusBar("BAT", status.batteryPercent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ToggleTile(
    label: String,
    on: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(3.dp, RectangleShape, clip = false)     // ← 加这行
            .background(if (on) ToggleOnBg else ToggleOffBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (on) Color.White else DashTextGray,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Box(
                Modifier
                    .size(5.dp)
                    .background(if (on) Color.White else Color(0x44FFFFFF))
            )
        }
    }
}

@Composable
private fun StatusBar(
    label: String,
    percent: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 9.sp, color = DashTextGray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Canvas(
            Modifier
                .weight(1f)
                .height(4.dp)
        ) {
            drawRect(BarTrack, size = size)
            val w = size.width * (percent.coerceIn(0, 100) / 100f)
            drawRect(
                color = barColorFor(label, percent),
                topLeft = Offset(0f, 0f),
                size = Size(w, size.height)
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$percent",
            fontSize = 9.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(22.dp)
        )
    }
}

private fun barColorFor(label: String, percent: Int): Color = when (label) {
    "BAT" -> when {
        percent < 20 -> BarRed
        percent < 50 -> BarYellow
        else -> BarGreen
    }
    else -> when {
        percent < 60 -> BarBlue
        percent < 80 -> BarYellow
        else -> BarRed
    }
}