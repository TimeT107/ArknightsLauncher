package com.example.arklauncher.components

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arklauncher.data.AppLauncher
import com.example.arklauncher.data.LauncherPreferences
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

// 配色
private val TerraBg = Color(0xFF1A2B3F)          // 泰拉：深蓝
private val TalosBg = Color(0xFF2A3A2A)          // 塔卫二：深绿
private val GameTextWhite = Color.White
private val GameTextGray = Color(0x99FFFFFF)
private val DiagonalLine = Color(0x88FFFFFF)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GamePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 读取已绑定的包名
    val terraFlow = remember(context) {
        LauncherPreferences.getBoundAppFlow(context, "game_terra")
    }
    val talosFlow = remember(context) {
        LauncherPreferences.getBoundAppFlow(context, "game_talos")
    }
    val terraPkg by terraFlow.collectAsState(initial = null)
    val talosPkg by talosFlow.collectAsState(initial = null)

    var showPicker by remember { mutableStateOf(false) }
    var pickerKey by remember { mutableStateOf("") }
    var pickerTitle by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {

        // ─── 斜线分割背景 ───
        Canvas(Modifier.fillMaxSize()) {
            // 左：泰拉（梯形，顶宽底窄）
            val leftPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.58f, 0f)
                lineTo(size.width * 0.42f, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(leftPath, TerraBg)

            // 右：塔卫二（梯形，顶窄底宽）
            val rightPath = Path().apply {
                moveTo(size.width * 0.58f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(size.width * 0.42f, size.height)
                close()
            }
            drawPath(rightPath, TalosBg)

            // 斜线
            drawLine(
                color = DiagonalLine,
                start = Offset(size.width * 0.58f, 0f),
                end = Offset(size.width * 0.42f, size.height),
                strokeWidth = 3f
            )
        }

        // ─── 点击区域（左右 50/50 分）───
        Row(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .combinedClickable(
                        onClick = {
                            if (terraPkg.isNullOrEmpty()) {
                                pickerKey = "game_terra"
                                pickerTitle = "选择「明日方舟」"
                                showPicker = true
                            } else {
                                AppLauncher.launchPackage(context, terraPkg)
                            }
                        },
                        onLongClick = {
                            pickerKey = "game_terra"
                            pickerTitle = "重新选择「明日方舟」"
                            showPicker = true
                        }
                    )
            )
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .combinedClickable(
                        onClick = {
                            if (talosPkg.isNullOrEmpty()) {
                                pickerKey = "game_talos"
                                pickerTitle = "选择「明日方舟：终末地」"
                                showPicker = true
                            } else {
                                AppLauncher.launchPackage(context, talosPkg)
                            }
                        },
                        onLongClick = {
                            pickerKey = "game_talos"
                            pickerTitle = "重新选择「明日方舟：终末地」"
                            showPicker = true
                        }
                    )
            )
        }

        // ─── 文字（覆盖在点击区之上，但 clickable 会穿透？不会——文字用 pointerInput 不拦截）───
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "泰拉",
                    fontSize = 22.sp,
                    color = GameTextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "明日方舟",
                    fontSize = 11.sp,
                    color = GameTextGray,
                    textAlign = TextAlign.Center
                )
            }
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "塔卫二",
                    fontSize = 22.sp,
                    color = GameTextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "终末地",
                    fontSize = 11.sp,
                    color = GameTextGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // ─── 首次点击的 AppPicker ───
    if (showPicker) {
        AppPickerDialog(
            title = pickerTitle,
            intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            onDismiss = { showPicker = false },
            onSelect = { app ->
                scope.launch {
                    LauncherPreferences.saveBoundApp(context, pickerKey, app.packageName)
                }
                showPicker = false
                AppLauncher.launchPackage(context, app.packageName)
            }
        )
    }
}