package com.example.arklauncher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.arklauncher.components.*
import com.example.arklauncher.data.LauncherState
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.runtime.mutableFloatStateOf
import com.example.arklauncher.data.MediaUiState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Offset
import android.os.SystemClock
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.arklauncher.service.PanelAccessibilityService
import com.example.arklauncher.ui.PanelPermissionDialog
import com.example.arklauncher.ui.isAccessibilityServiceEnabled
import androidx.compose.runtime.mutableLongStateOf


@Composable
fun TabletHome(onOpenAppList: (Offset) -> Unit = {}) {
    LaunchedEffect(Unit) {
        while (true) {
            LauncherState.update()
            delay(60_000L)
        }
    }

    val context = LocalContext.current
    val config = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { config.screenHeightDp.dp.toPx() }
    val topZonePx = screenHeightPx / 3f          // 顶部 1/3 区域

    var showPermissionDialog by remember { mutableStateOf(false) }

    // 记录手势起点
    var dragStartX by remember { mutableFloatStateOf(0f) }
    var dragStartY by remember { mutableFloatStateOf(0f) }
    var triggeredThisDrag by remember { mutableStateOf(false) }
    var lastTriggerTime by remember { mutableLongStateOf(0L) }

    val tiltState = rememberTilt(
        xSensitivity = 2.0f,
        ySensitivity = 0.8f,
        smoothing = 0.90f,
        deadZone = 0.04f
    )

    val density = LocalDensity.current
    val maxShiftPx = with(density) { 14.dp.toPx() }
    val panelBaseOffsetPx = with(density) { (-20).dp.toPx() }
    val assistantBaseOffsetPx = with(density) { (-40).dp.toPx() }
    var terminalBottomPx by remember { mutableFloatStateOf(0f) }
    val density2 = LocalDensity.current
    val expandedTopDp = with(density2) { terminalBottomPx.toDp() }
    val arkBottomPadding by animateDpAsState(
        targetValue = if (MediaUiState.isExpanded.value) 210.dp else 110.dp,
        animationSpec = tween(durationMillis = 300),
        label = "arkBottomPadding"
    )

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                        dragStartY = offset.y
                        triggeredThisDrag = false
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (triggeredThisDrag) return@detectVerticalDragGestures
                        // 只处理从顶部 1/3 区域开始、向下滑的手势
                        if (dragStartY > topZonePx) return@detectVerticalDragGestures
                        // 只处理累计下滑超过 60dp
                        val currentY = change.position.y
                        val deltaY = currentY - dragStartY
                        if (deltaY < 60.dp.toPx()) return@detectVerticalDragGestures

                        // 防止短时间内重复触发
                        val now = SystemClock.elapsedRealtime()
                        if (now - lastTriggerTime < 800) {
                            triggeredThisDrag = true
                            return@detectVerticalDragGestures
                        }

                        // 判断左/右
                        val isLeftSide = dragStartX < size.width / 2f

                        // 检查权限
                        if (!isAccessibilityServiceEnabled(context)) {
                            triggeredThisDrag = true
                            showPermissionDialog = true
                            return@detectVerticalDragGestures
                        }

                        // 触发
                        val svc = PanelAccessibilityService.instance
                        if (svc != null) {
                            if (isLeftSide) {
                                svc.openNotifications()
                            } else {
                                svc.openQuickSettings()
                            }
                            lastTriggerTime = now
                            triggeredThisDrag = true
                        } else {
                            // Service 没连上，弹对话框
                            triggeredThisDrag = true
                            showPermissionDialog = true
                        }
                    },
                    onDragEnd = {
                        triggeredThisDrag = false
                    },
                    onDragCancel = {
                        triggeredThisDrag = false
                    }
                )
            }
    ) {

        // 背景
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val t = tiltState.value
                    val sx = t.x * maxShiftPx
                    val sy = t.y * maxShiftPx
                    translationX = -sx * 0.4f
                    translationY = -sy * 0.4f
                }
        ) {
            BackgroundLayer(mode = LauncherState.status.value.dayMode)
        }

        // 助理
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.55f)
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    val t = tiltState.value
                    val sx = t.x * maxShiftPx
                    val sy = t.y * maxShiftPx
                    translationX = assistantBaseOffsetPx + sx * 0.25f
                    translationY = sy * 0.15f
                }
                .padding(top = 40.dp, bottom = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            AssistantView(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 右侧板块
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.56f)
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    val t = tiltState.value
                    val sx = t.x * maxShiftPx
                    val sy = t.y * maxShiftPx
                    translationX = panelBaseOffsetPx + sx * 1.8f
                    translationY = sy * 0.3f
                }
                .padding(top = 80.dp, bottom = arkBottomPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPanel(status = LauncherState.status.value)
                ArkTerminal(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            terminalBottomPx = coords.positionInRoot().y + coords.size.height
                        },
                    onTerminalClick = onOpenAppList
                )
            }
        }

        // Dock
        // BreakingNews 条，紧贴 Dock 上方
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            QuickBar()
        }

        // ⭐ 权限对话框
        if (showPermissionDialog) {
            PanelPermissionDialog(
                onDismiss = { showPermissionDialog = false },
                onConfirm = { }
            )
        }

        // BreakingNews 条：在 QuickBar 之后声明 → z-order 在其之上
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 86.dp)
        ) {
            BreakingNewsBar()
        }

        // ⭐ 展开的媒体板块：在 ArkTerminal 下方、Dock 上方
        androidx.compose.animation.AnimatedVisibility(
            visible = MediaUiState.isExpanded.value,
            enter = androidx.compose.animation.fadeIn() +
                    androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }),
            exit = androidx.compose.animation.fadeOut() +
                    androidx.compose.animation.slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 110.dp)          // 底部避开 Dock
        ) {
            MediaExpandedPanel()
        }
    }
}