package com.example.arklauncher.ui

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.LaunchedEffect

@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp
    val isTablet = config.smallestScreenWidthDp >= 600

    var inAppList by remember { mutableStateOf(false) }
    var targetInAppList by remember { mutableStateOf(false) }
    var revealCenter by remember { mutableStateOf(Offset.Zero) }
    var isAnimating by remember { mutableStateOf(false) }
    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    val revealAnim = remember { Animatable(0f) }

    val maxRadius = remember(revealCenter, screenSize) {
        if (screenSize == IntSize.Zero) 0f
        else {
            val w = screenSize.width.toFloat()
            val h = screenSize.height.toFloat()
            listOf(
                Offset(0f, 0f),
                Offset(w, 0f),
                Offset(0f, h),
                Offset(w, h)
            ).maxOf { (it - revealCenter).getDistance() }
        }
    }

    // ⭐ 底层 Home 只在"当前在 AppList 或正在进入"时模糊
    val shouldBlurHome = inAppList || (isAnimating && targetInAppList)
    val blurModifier = if (shouldBlurHome && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(20.dp, BlurredEdgeTreatment.Unbounded)
    } else Modifier

    val renderHome: @Composable () -> Unit = {
        if (isTablet || isLandscape) {
            TabletHome(onOpenAppList = { pos ->
                if (!isAnimating) {
                    revealCenter = pos
                    targetInAppList = true
                    isAnimating = true
                    scope.launch {
                        revealAnim.snapTo(0f)
                        revealAnim.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
                        inAppList = true
                        // ⭐ 等一帧，避免同帧切层导致的闪烁
                        withFrameNanos { }
                        isAnimating = false
                    }
                }
            })
        } else {
            PhoneHome(onOpenAppList = { pos ->
                if (!isAnimating) {
                    revealCenter = pos
                    targetInAppList = true
                    isAnimating = true
                    scope.launch {
                        revealAnim.snapTo(0f)
                        revealAnim.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
                        inAppList = true
                        withFrameNanos { }
                        isAnimating = false
                    }
                }
            })
        }
    }

    val closeAppList: (Offset) -> Unit = { pos ->
        if (!isAnimating) {
            revealCenter = pos
            targetInAppList = false
            isAnimating = true
            scope.launch {
                revealAnim.snapTo(0f)
                revealAnim.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
                inAppList = false
                // ⭐ 等一帧，让底层 unblur 完成后再撤销揭示层
                withFrameNanos { }
                isAnimating = false
            }
        }
    }

    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    // 主界面隐藏状态栏，AppList 显示
    LaunchedEffect(inAppList) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            if (inAppList) {
                controller.show(WindowInsetsCompat.Type.statusBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            } else {
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { screenSize = it }
    ) {
        // ═══ 底层：Home，永不切换内容，只切换模糊状态 ═══
        Box(
            Modifier
                .fillMaxSize()
                .then(blurModifier)
        ) {
            renderHome()
        }

        // ═══ AppList 层：单独一层，永远清晰 ═══
        val showAppList = inAppList || (isAnimating && targetInAppList)
        if (showAppList) {
            val radius = if (isAnimating && targetInAppList) {
                revealAnim.value * maxRadius       // 打开动画中：半径增长
            } else {
                maxRadius                          // 稳定态或关闭动画中：全屏
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        if (radius >= maxRadius - 0.5f || maxRadius <= 0f) {
                            drawContent()
                        } else {
                            val path = Path().apply {
                                addOval(
                                    Rect(
                                        offset = Offset(
                                            revealCenter.x - radius,
                                            revealCenter.y - radius
                                        ),
                                        size = Size(radius * 2, radius * 2)
                                    )
                                )
                            }
                            clipPath(path) {
                                this@drawWithContent.drawContent()
                            }
                        }
                    }
            ) {
                AppListScreen(onBack = closeAppList)
            }
        }

        // ═══ Home 揭示层：只在关闭动画中显示（Home 从圆扩散覆盖 AppList） ═══
        if (isAnimating && !targetInAppList && inAppList) {
            val radius = revealAnim.value * maxRadius
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val path = Path().apply {
                            addOval(
                                Rect(
                                    offset = Offset(
                                        revealCenter.x - radius,
                                        revealCenter.y - radius
                                    ),
                                    size = Size(radius * 2, radius * 2)
                                )
                            )
                        }
                        clipPath(path) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                renderHome()
            }
        }
    }
}