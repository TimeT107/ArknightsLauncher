package com.example.arklauncher.components

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arklauncher.data.AppLauncher
import com.example.arklauncher.data.DockApp
import com.example.arklauncher.data.DockManager
import com.example.arklauncher.data.LauncherPreferences
import com.example.arklauncher.ui.AppPicker
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay
import com.example.arklauncher.data.MediaUiState
import kotlinx.coroutines.launch

private val DockBg = Color(0xFF3A3A3A)
private val DockPressedBg = Color(0xFF000000)
private val FriendBg = Color(0xFF3A3A3A)
private val ArchiveBg = Color(0xFFE8E8E8)
private val IndicatorActive = Color(0xFFFF6B1A)
private val IndicatorInactive = Color(0xFFFFFFFF)

private const val PAGE_COUNT = 4
private const val AUTO_ROTATE_INTERVAL_MS = 5_000L
private const val USER_PAUSE_MS = 8_000L

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickBar() {
    val context = LocalContext.current
    val view = LocalView.current
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })

    var showPicker by remember { mutableStateOf(false) }
    var currentSlot by remember { mutableStateOf("") }

    val dock1 = rememberDockInfo("dock_1")
    val dock2 = rememberDockInfo("dock_2")
    val dock3 = rememberDockInfo("dock_3")
    val dock4 = rememberDockInfo("dock_4")

    // 好友 / 档案 绑定的应用
    val scope = rememberCoroutineScope()
    val friendPkg by rememberBoundPkg(context, "friend")
    val messagePkg by rememberBoundPkg(context, "message")

    var bindPickerKey by remember { mutableStateOf<String?>(null) }
    var bindPickerTitle by remember { mutableStateOf("") }
    var bindPickerIntent by remember { mutableStateOf<android.content.Intent?>(null) }

    fun launchOrPick(
        key: String,
        title: String,
        boundPkg: String?,
        intent: android.content.Intent
    ) {
        if (!boundPkg.isNullOrEmpty()) {
            AppLauncher.launchPackage(context, boundPkg)
        } else {
            bindPickerKey = key
            bindPickerTitle = title
            bindPickerIntent = intent
        }
    }

    val friendIntent = remember {
        android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_APP_CONTACTS)
    }
    val messageIntent = remember {
        android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_APP_MESSAGING)
    }

    // 用户拖动时暂停自动轮换
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var lastUserInteraction by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isDragged) {
        if (isDragged) lastUserInteraction = System.currentTimeMillis()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(AUTO_ROTATE_INTERVAL_MS)
            val timeSince = System.currentTimeMillis() - lastUserInteraction
            if (timeSince > USER_PAUSE_MS) {
                val next = (pagerState.currentPage + 1) % PAGE_COUNT
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 0.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // ════════ 左侧：Dock 独立矩形 ════════
        Column(
            modifier = Modifier
                .weight(1f)
                .background(DockBg)
                .padding(bottom = 6.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) { page ->
                when (page) {
                    0 -> Row(Modifier.fillMaxSize()) {
                        DockSlot(
                            icon = dock1?.icon, defaultIcon = "☎",
                            name = dock1?.name ?: "电话",
                            onClickWithBounds = { rect ->
                                AppLauncher.launchPackageWithScaleUp(
                                    context, dock1?.packageName, view, rect
                                )
                            },
                            onLongClick = { currentSlot = "dock_1"; showPicker = true }
                        )
                        DockSlot(
                            icon = dock2?.icon, defaultIcon = "📷",
                            name = dock2?.name ?: "相机",
                            onClickWithBounds = { rect ->
                                AppLauncher.launchPackageWithScaleUp(
                                    context, dock2?.packageName, view, rect
                                )
                            },
                            onLongClick = { currentSlot = "dock_2"; showPicker = true }
                        )
                        DockSlot(
                            icon = dock3?.icon, defaultIcon = "🖼",
                            name = dock3?.name ?: "相册",
                            onClickWithBounds = { rect ->
                                AppLauncher.launchPackageWithScaleUp(
                                    context, dock3?.packageName, view, rect
                                )
                            },
                            onLongClick = { currentSlot = "dock_3"; showPicker = true }
                        )
                        DockSlot(
                            icon = dock4?.icon, defaultIcon = "♪",
                            name = dock4?.name ?: "音乐",
                            onClickWithBounds = { rect ->
                                AppLauncher.launchPackageWithScaleUp(
                                    context, dock4?.packageName, view, rect
                                )
                            },
                            onLongClick = { currentSlot = "dock_4"; showPicker = true }
                        )
                    }
                    1 -> {
                        if (MediaUiState.isExpanded.value) {
                            EmptyPage(2)
                        } else {
                            MediaPlayerPage(modifier = Modifier.fillMaxSize())
                        }
                    }
                    2 -> DashboardPage(modifier = Modifier.fillMaxSize())
                    3 -> GamePage(modifier = Modifier.fillMaxSize())
                    else -> EmptyPage(page + 1)
                }
            }

            PagerIndicator(pagerState)
        }

        Spacer(Modifier.width(8.dp))

        // ════════ 右侧：好友 / 档案 两个独立矩形 ════════
        Column(
            modifier = Modifier
                .width(110.dp)
                .padding(bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(FriendBg)
                    .combinedClickable(
                        onClick = {
                            launchOrPick("friend", "选择通讯录应用", friendPkg, friendIntent)
                        },
                        onLongClick = {
                            bindPickerKey = "friend"
                            bindPickerTitle = "重新选择通讯录应用"
                            bindPickerIntent = friendIntent
                        }
                    )
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(
                        id = com.example.arklauncher.R.drawable.friend_icon
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 0.dp)
                )
                Text(
                    "好友",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFont.hkstw12,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(ArchiveBg)
                    .combinedClickable(
                        onClick = {
                            launchOrPick("message", "选择信息应用", messagePkg, messageIntent)
                        },
                        onLongClick = {
                            bindPickerKey = "message"
                            bindPickerTitle = "重新选择信息应用"
                            bindPickerIntent = messageIntent
                        }
                    )
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(
                        id = com.example.arklauncher.R.drawable.archive_icon
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 0.dp)
                )
                Text(
                    "档案",
                    fontSize = 16.sp,
                    color = Color(0xFF141414),
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFont.hkstw12,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                )
            }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            text = {
                AppPicker(slot = currentSlot) { showPicker = false }
            },
            confirmButton = {}
        )
    }

    // 好友/档案 首次点击的选择器
    if (bindPickerKey != null && bindPickerIntent != null) {
        AppPickerDialog(
            title = bindPickerTitle,
            intent = bindPickerIntent!!,
            onDismiss = { bindPickerKey = null; bindPickerIntent = null },
            onSelect = { app ->
                val key = bindPickerKey!!
                scope.launch {
                    LauncherPreferences.saveBoundApp(context, key, app.packageName)
                }
                bindPickerKey = null
                bindPickerIntent = null
                AppLauncher.launchPackage(context, app.packageName)
            }
        )
    }
}

@Composable
private fun EmptyPage(pageNumber: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "第 $pageNumber 页",
            fontSize = 12.sp,
            color = Color(0x55FFFFFF)
        )
    }
}

@Composable
private fun PagerIndicator(pagerState: PagerState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(PAGE_COUNT) { index ->
            val selected = pagerState.currentPage == index
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(2.dp)
                    .width(if (selected) 18.dp else 8.dp)
                    .background(if (selected) IndicatorActive else IndicatorInactive)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.DockSlot(
    icon: Drawable?,
    defaultIcon: String,
    name: String,
    onClickWithBounds: (Rect) -> Unit,       // ← 改签名
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    var slotBounds by remember { mutableStateOf(Rect()) }

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                val size = coords.size
                slotBounds = Rect(
                    pos.x.toInt(),
                    pos.y.toInt(),
                    (pos.x + size.width).toInt(),
                    (pos.y + size.height).toInt()
                )
            }
            .background(if (pressed) DockPressedBg else Color.Transparent)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClickWithBounds(slotBounds) },
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Image(
                    painter = rememberDrawablePainter(icon),
                    contentDescription = name,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(text = defaultIcon, fontSize = 28.sp, color = Color.White)
            }
            Text(
                text = name,
                fontSize = 11.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 64.dp)
            )
        }
    }
}

@Composable
fun rememberDockPackage(slot: String): String? {
    val context = LocalContext.current
    val flow = remember(context, slot) {
        LauncherPreferences.getDockAppFlow(context, slot)
    }
    return flow.collectAsState(initial = null).value
}

@Composable
fun rememberDockInfo(slot: String): DockApp? {
    val context = LocalContext.current
    val packageName = rememberDockPackage(slot)
    return remember(context, packageName) {
        DockManager.getDockApp(context, packageName)
    }
}

@Composable
private fun rememberBoundPkg(
    context: android.content.Context,
    key: String
): State<String?> {
    val flow = remember(context, key) {
        LauncherPreferences.getBoundAppFlow(context, key)
    }
    return flow.collectAsState(initial = null)
}