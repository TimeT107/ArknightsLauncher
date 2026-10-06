package com.example.arklauncher.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.arklauncher.R
import com.example.arklauncher.data.AppInfo
import com.example.arklauncher.data.AppLauncher
import com.example.arklauncher.data.AppManager
import com.example.arklauncher.data.DayMode
import com.example.arklauncher.data.LauncherState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.statusBarsPadding
import kotlinx.coroutines.flow.firstOrNull
import com.example.arklauncher.components.AppPickerDialog
import com.example.arklauncher.data.LauncherPreferences
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke





// ═══════════════ 配色 ═══════════════
private data class AppListColors(
    val bg: Color,
    val topBar: Color,
    val blockBg: Color,
    val blockText: Color,
    val blockDivider: Color,
    val searchHint: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val menuBg: Color,
    val menuText: Color,
    val menuDivider: Color,
    val accent: Color
)

private val DayAppList = AppListColors(
    bg = Color(0x99F5F5F5),
    topBar = Color(0xFFFFFFFF),
    blockBg = Color(0xFF3A3A3A),
    blockText = Color.White,
    blockDivider = Color(0x33FFFFFF),
    searchHint = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF141414),
    textSecondary = Color(0xFF6E6E6E),
    menuBg = Color(0xFFFFFFFF),
    menuText = Color(0xFF141414),
    menuDivider = Color(0xFFE0E0E0),
    accent = Color(0xFFFF6B1A)
)

private val NightAppList = AppListColors(
    bg = Color(0x99141414),
    topBar = Color(0xFF0A0A0A),
    blockBg = Color(0xFF3A3A3A),
    blockText = Color.White,
    blockDivider = Color(0x33FFFFFF),
    searchHint = Color(0xFFFFFFFF),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFAAAAAA),
    menuBg = Color(0xFF2A2A2A),
    menuText = Color(0xFFFFFFFF),
    menuDivider = Color(0xFF3A3A3A),
    accent = Color(0xFFFF6B1A)
)

private fun appListColorsFor(dayMode: DayMode): AppListColors =
    if (dayMode == DayMode.DAY) DayAppList else NightAppList

// ═══════════════ 排序方式 ═══════════════
enum class SortMode { RECENT, FREQUENT, NAME, INSTALL }

private fun sortLabel(mode: SortMode): String = when (mode) {
    SortMode.RECENT -> "最近"
    SortMode.FREQUENT -> "常用"
    SortMode.NAME -> "名称"
    SortMode.INSTALL -> "安装"
}

// ═══════════════ 主界面 ═══════════════
@Composable
fun AppListScreen(onBack: (Offset) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = appListColorsFor(LauncherState.status.value.dayMode)

    // ⭐ 从缓存初始化，避免二次进入闪空列表
    var apps by remember {
        mutableStateOf(AppManager.getCachedUsageApps() ?: emptyList())
    }
    var query by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(SortMode.RECENT) }
    var isSearching by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }

    var showHomePanel by remember { mutableStateOf(false) }
    var panelPickerKey by remember { mutableStateOf<String?>(null) }
    var panelPickerTitle by remember { mutableStateOf("") }
    var panelPickerIntent by remember { mutableStateOf<Intent?>(null) }

    var menuApp by remember { mutableStateOf<AppInfo?>(null) }
    var menuCenter by remember { mutableStateOf(Offset.Zero) }
    var menuIconSize by remember { mutableFloatStateOf(0f) }

    // ⭐ 返回按钮在屏幕上的中心位置（用于圆形揭示动画圆心）
    var backButtonCenter by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        if (AppManager.getCachedUsageApps() == null) {
            apps = withContext(Dispatchers.IO) {
                AppManager.getAppsWithUsage(context)
            }
        }
    }
    val filtered = remember(apps, query, sortMode) {
        val base = if (query.isBlank()) apps
        else apps.filter { it.name.contains(query, ignoreCase = true) }
        when (sortMode) {
            SortMode.RECENT -> base.sortedByDescending { it.lastUsedTime }
            SortMode.FREQUENT -> base.sortedByDescending { it.usageSeconds }
            SortMode.NAME -> {
                val collator = java.text.Collator.getInstance(java.util.Locale.CHINA)
                base.sortedWith { a, b -> collator.compare(a.name, b.name) }
            }
            SortMode.INSTALL -> base.sortedByDescending { it.firstInstallTime }
        }
    }

    // 系统返回键：以返回按钮位置为圆心执行收缩动画
    BackHandler {
        val center = if (backButtonCenter == Offset.Zero) {
            Offset(
                x = context.resources.displayMetrics.widthPixels / 2f,
                y = 40f
            )
        } else backButtonCenter
        onBack(center)
    }

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            Column(Modifier.fillMaxSize()) {
                TopBar(
                    query = query,
                    onQueryChange = { query = it },
                    onBack = onBack,
                    onBackCenterChanged = { backButtonCenter = it },
                    onHomeClick = { showHomePanel = !showHomePanel },
                    sortMode = sortMode,
                    onSortChange = { sortMode = it },
                    colors = colors,
                    isSearching = isSearching,
                    onSearchingChange = { isSearching = it },
                    isRefreshing = isRefreshing,                          // ⭐ 新增
                    onRefresh = {                                          // ⭐ 新增
                        if (!isRefreshing) {
                            scope.launch {
                                isRefreshing = true
                                apps = withContext(Dispatchers.IO) {
                                    AppManager.refreshAppsWithUsage(context)
                                }
                                delay(500)
                                isRefreshing = false
                            }
                        }
                    }
                )

                // ⭐ 顶栏下的柔和阴影
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x88000000),      // 顶部最浓
                                    Color(0x44000000),      // 中间
                                    Color.Transparent       // 底部透明
                                )
                            )
                        )
                )

                Box(Modifier.weight(1f)) {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        items(filtered, key = { it.packageName }) { app ->
                            AppGridItem(
                                app = app,
                                colors = colors,
                                onClick = {
                                    AppLauncher.launchPackage(context, app.packageName)
                                },
                                onLongClick = { center, iconSize ->
                                    menuApp = app
                                    menuCenter = center
                                    menuIconSize = iconSize
                                }
                            )
                        }
                    }

                    // 搜索状态下的点击遮罩
                    if (isSearching) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isSearching = false }
                        )
                    }
                }
            }
        }

        menuApp?.let { app ->
            LongPressOverlay(
                app = app,
                center = menuCenter,
                iconSizePx = menuIconSize,
                colors = colors,
                onDismiss = { menuApp = null },
                onLaunchApp = {
                    AppLauncher.launchPackage(context, app.packageName)
                },
                onAppInfo = {
                    openAppInfo(context, app.packageName)
                },
                onUninstall = {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (_: Exception) {}
                }
            )
        }

        // ⭐ 点击外部关闭遮罩（在面板下方）
        if (showHomePanel) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showHomePanel = false }
            )
        }

        // ⭐ 主页大面板（在搜索/排序之上，返回/主页之下）
        AnimatedVisibility(
            visible = showHomePanel,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeIn(tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            HomePanel(
                modifier = Modifier.fillMaxWidth(),
                onNodeClick = { id ->
                    showHomePanel = false
                    handlePanelNodeClick(
                        context = context,
                        id = id,
                        openPicker = { key, title, intent ->
                            panelPickerKey = key
                            panelPickerTitle = title
                            panelPickerIntent = intent
                        }
                    )
                }
            )
        }

        // ⭐ 返回/主页覆盖层（只在面板打开时显示，在最上层）
        if (showHomePanel) {
            BackHomeOverlay(
                onBack = onBack,
                onBackCenterChanged = { backButtonCenter = it },
                onHome = { showHomePanel = false }
            )
        }

        // ⭐ 面板节点的应用选择器
        if (panelPickerKey != null && panelPickerIntent != null) {
            AppPickerDialog(
                title = panelPickerTitle,
                intent = panelPickerIntent!!,
                onDismiss = {
                    panelPickerKey = null
                    panelPickerIntent = null
                },
                onSelect = { app ->
                    val key = panelPickerKey!!
                    scope.launch {
                        LauncherPreferences.saveBoundApp(context, key, app.packageName)
                    }
                    panelPickerKey = null
                    panelPickerIntent = null
                    AppLauncher.launchPackage(context, app.packageName)
                }
            )
        }
    }
}



// ═══════════════ 顶部栏 ═══════════════
@Composable
private fun TopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: (Offset) -> Unit,
    onBackCenterChanged: (Offset) -> Unit,
    onHomeClick: () -> Unit,
    sortMode: SortMode,
    onSortChange: (SortMode) -> Unit,
    colors: AppListColors,
    isSearching: Boolean,
    onSearchingChange: (Boolean) -> Unit,
    isRefreshing: Boolean,                       // ⭐ 新增
    onRefresh: () -> Unit                         // ⭐ 新增
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val leftBlockWidth by animateDpAsState(
        targetValue = if (isSearching) 0.dp else 174.dp,
        animationSpec = tween(280),
        label = "leftBlockWidth"
    )
    val leftSpacerWidth by animateDpAsState(
        targetValue = if (isSearching) 0.dp else 8.dp,
        animationSpec = tween(280),
        label = "leftSpacerWidth"
    )

    LaunchedEffect(isSearching) {
        if (isSearching) {
            delay(150)
            focusRequester.requestFocus()
        } else {
            keyboard?.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.topBar)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ═══ 左板块 ═══
            Box(
                Modifier
                    .width(leftBlockWidth)
                    .height(24.dp)
                    .clipToBounds()
            ) {
                Box(
                    Modifier
                        .width(174.dp)
                        .height(24.dp)
                        .graphicsLayer {
                            translationX = -(174.dp.toPx() - leftBlockWidth.toPx())
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.topbar_left),
                        contentDescription = null,
                        modifier = Modifier
                            .width(174.dp)
                            .height(24.dp),
                        contentScale = ContentScale.FillBounds
                    )

                    Row(Modifier.matchParentSize()) {
                        var backBoxCenter by remember { mutableStateOf(Offset.Zero) }
                        Box(
                            Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .onGloballyPositioned { coords ->
                                    val bounds = coords.boundsInWindow()
                                    backBoxCenter = Offset(
                                        bounds.left + bounds.width / 2f,
                                        bounds.top + bounds.height / 2f
                                    )
                                    onBackCenterChanged(backBoxCenter)
                                }
                                .clickable { onBack(backBoxCenter) }
                        )
                        Box(
                            Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                                .clickable { onHomeClick() }
                        )
                    }
                }
            }

            Spacer(Modifier.width(leftSpacerWidth))

            // ═══ 搜索区 ═══
            Box(
                Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.blockBg)
                    .then(
                        if (!isSearching) {
                            Modifier.clickable { onSearchingChange(true) }
                        } else Modifier
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isSearching) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = colors.blockText,
                            fontSize = 11.sp
                        ),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                } else {
                    SearchIcon(color = colors.searchHint, size = 14.dp)
                }
            }

            Spacer(Modifier.width(8.dp))

            // ⭐ 刷新按钮
            RefreshButton(
                isRefreshing = isRefreshing,
                colors = colors,
                onClick = onRefresh
            )

            Spacer(Modifier.width(8.dp))

            // ═══ 右板块：排序 ═══
            Row(
                Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.blockBg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SortTab("最近", sortMode == SortMode.RECENT, colors) {
                    onSortChange(SortMode.RECENT)
                }

                Box(
                    Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(colors.blockDivider)
                )

                SortDropdown(
                    currentMode = sortMode,
                    colors = colors,
                    onSelect = onSortChange
                )
            }
        }




        // ⭐ 状态栏区域阴影：从屏幕顶向下渐隐
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding() + 6.dp
                )
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66000000),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun SortTab(
    label: String,
    selected: Boolean,
    colors: AppListColors,
    onClick: () -> Unit
) {
    Text(
        text = label,
        color = if (selected) Color(0xFF4FC3F7) else colors.blockText,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}

@Composable
private fun SortDropdown(
    currentMode: SortMode,
    colors: AppListColors,
    onSelect: (SortMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isRecentActive = currentMode == SortMode.RECENT
    val displayMode = if (isRecentActive) SortMode.FREQUENT else currentMode

    Box {
        Row(
            Modifier
                .clickable { expanded = true }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sortLabel(displayMode),
                color = if (isRecentActive) colors.blockText else Color(0xFF4FC3F7),
                fontSize = 12.sp,
                fontWeight = if (isRecentActive) FontWeight.Normal else FontWeight.Bold
            )
            Spacer(Modifier.width(4.dp))
            Text("▾", color = colors.searchHint, fontSize = 10.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.menuBg)
        ) {
            SortMenuItem("常用", currentMode == SortMode.FREQUENT, colors) {
                onSelect(SortMode.FREQUENT); expanded = false
            }
            SortMenuItem("名称", currentMode == SortMode.NAME, colors) {
                onSelect(SortMode.NAME); expanded = false
            }
            SortMenuItem("安装时间", currentMode == SortMode.INSTALL, colors) {
                onSelect(SortMode.INSTALL); expanded = false
            }
        }
    }
}

@Composable
private fun SortMenuItem(
    label: String,
    selected: Boolean,
    colors: AppListColors,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                color = if (selected) Color(0xFF4FC3F7) else colors.menuText,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        onClick = onClick
    )
}

// ═══════════════ 网格项 ═══════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppGridItem(
    app: AppInfo,
    colors: AppListColors,
    onClick: () -> Unit,
    onLongClick: (center: Offset, iconSizePx: Float) -> Unit
) {
    val density = LocalDensity.current
    val view = androidx.compose.ui.platform.LocalView.current
    val context = LocalContext.current

    val smallIcon = remember(app.packageName) {
        app.icon.toBitmap(128, 128).asImageBitmap()
    }

    var itemTopLeft by remember { mutableStateOf(Offset.Zero) }
    var itemWidth by remember { mutableFloatStateOf(0f) }
    var itemHeight by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .width(120.dp)
            .fillMaxHeight()
            .onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                val size = coords.size
                itemTopLeft = Offset(pos.x, pos.y)
                itemWidth = size.width.toFloat()
                itemHeight = size.height.toFloat()
            }
            .shadow(2.dp, RectangleShape, clip = false)
            .border(1.dp, colors.blockDivider, RectangleShape)
            .combinedClickable(
                onClick = {
                    // ⭐ 用带放大动画的启动
                    val rect = android.graphics.Rect(
                        itemTopLeft.x.toInt(),
                        itemTopLeft.y.toInt(),
                        (itemTopLeft.x + itemWidth).toInt(),
                        (itemTopLeft.y + itemHeight).toInt()
                    )
                    AppLauncher.launchPackageWithScaleUp(
                        context, app.packageName, view, rect
                    )
                },
                onLongClick = {
                    val center = Offset(
                        itemTopLeft.x + itemWidth / 2,
                        itemTopLeft.y + itemHeight / 2
                    )
                    val iconPx = with(density) { 56.dp.toPx() }
                    onLongClick(center, iconPx)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                bitmap = smallIcon,
                contentDescription = app.name,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = app.name,
                fontSize = 10.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════ 长按覆盖层 ═══════════════
@Composable
private fun LongPressOverlay(
    app: AppInfo,
    center: Offset,
    iconSizePx: Float,
    colors: AppListColors,
    onDismiss: () -> Unit,
    onLaunchApp: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val anim = remember { Animatable(0f) }
    var closing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        anim.animateTo(1f, tween(durationMillis = 220))
    }

    val dismiss: () -> Unit = {
        if (!closing) {
            closing = true
            scope.launch {
                anim.animateTo(0f, tween(durationMillis = 180))
                onDismiss()
            }
        }
    }

    val dimAlpha = anim.value * 0.66f
    val iconSizeDp = with(density) { iconSizePx.toDp() }
    val scaledIconSizeDp = iconSizeDp * (1f + 0.5f * anim.value)

    val shortcuts by rememberShortcuts(context, app.packageName)

    val menuWidth = 220.dp
    val menuWidthPx = with(density) { menuWidth.toPx() }

    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    val menuLeftPx = (center.x - menuWidthPx / 2)
        .coerceIn(16f, screenWidthPx - menuWidthPx - 16f)

    var menuHeightPx by remember { mutableFloatStateOf(0f) }

    val finalIconSizePx = iconSizePx * 1.5f
    val iconBottomPx = center.y + finalIconSizePx / 2
    val iconTopPx = center.y - finalIconSizePx / 2
    val gapPx = with(density) { 8.dp.toPx() }
    val estimatedMenuHeightPx = with(density) { 300.dp.toPx() }
    val showAbove = (iconBottomPx + gapPx + estimatedMenuHeightPx) > screenHeightPx

    val menuTopPx = if (showAbove) {
        if (menuHeightPx > 0f) iconTopPx - gapPx - menuHeightPx
        else iconTopPx - gapPx - estimatedMenuHeightPx
    } else {
        iconBottomPx + gapPx
    }

    Box(
        Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = dismiss
            )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha))
        )

        val iconSizePxScaled = with(density) { scaledIconSizeDp.toPx() }
        val smallIcon = remember(app.packageName) {
            app.icon.toBitmap(256, 256).asImageBitmap()
        }

        Image(
            bitmap = smallIcon,
            contentDescription = app.name,
            modifier = Modifier
                .offset {
                    IntOffset(
                        (center.x - iconSizePxScaled / 2).toInt(),
                        (center.y - iconSizePxScaled / 2).toInt()
                    )
                }
                .size(scaledIconSizeDp)
                .shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
        )

        Column(
            modifier = Modifier
                .offset { IntOffset(menuLeftPx.toInt(), menuTopPx.toInt()) }
                .width(menuWidth)
                .onGloballyPositioned { coords ->
                    menuHeightPx = coords.size.height.toFloat()
                }
                .graphicsLayer {
                    alpha = anim.value
                    scaleX = 0.6f + 0.4f * anim.value
                    scaleY = 0.6f + 0.4f * anim.value
                    transformOrigin = if (showAbove) {
                        TransformOrigin(0.5f, 1f)
                    } else {
                        TransformOrigin(0.5f, 0f)
                    }
                }
                .shadow(8.dp, RoundedCornerShape(12.dp), clip = false)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.menuBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* 阻止穿透 */ }
                )
                .padding(vertical = 6.dp)
        ) {
            if (shortcuts.isNotEmpty()) {
                shortcuts.take(4).forEach { shortcut ->
                    ShortcutRow(
                        shortcut = shortcut,
                        colors = colors,
                        onClick = {
                            try {
                                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE)
                                        as android.content.pm.LauncherApps
                                launcherApps.startShortcut(
                                    app.packageName,
                                    shortcut.id,
                                    null,
                                    null,
                                    android.os.Process.myUserHandle()
                                )
                            } catch (_: Exception) {}
                            dismiss()
                        }
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.menuDivider)
                )
            }

            MenuRow("启动", colors) { dismiss(); onLaunchApp() }
            MenuRow("应用信息", colors) { dismiss(); onAppInfo() }
            MenuRow("卸载", colors) { dismiss(); onUninstall() }
        }
    }
}

@Composable
private fun ShortcutRow(
    shortcut: android.content.pm.ShortcutInfo,
    colors: AppListColors,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val icon = remember(shortcut.id) {
        try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE)
                    as android.content.pm.LauncherApps
            val dpi = (density.density * 160).toInt()
            launcherApps.getShortcutIconDrawable(shortcut, dpi)
                ?.toBitmap(64, 64)?.asImageBitmap()
        } catch (_: Exception) { null }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = shortcut.shortLabel?.toString() ?: "",
            color = colors.menuText,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun rememberShortcuts(
    context: Context,
    packageName: String
): State<List<android.content.pm.ShortcutInfo>> {
    val state = remember(packageName) {
        mutableStateOf<List<android.content.pm.ShortcutInfo>>(emptyList())
    }
    LaunchedEffect(packageName) {
        state.value = withContext(Dispatchers.IO) {
            try {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE)
                        as android.content.pm.LauncherApps
                val query = android.content.pm.LauncherApps.ShortcutQuery().apply {
                    setPackage(packageName)
                    setQueryFlags(
                        android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                                android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                    )
                }
                launcherApps.getShortcuts(query, android.os.Process.myUserHandle())
                    ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
    return state
}

@Composable
private fun MenuRow(label: String, colors: AppListColors, onClick: () -> Unit) {
    Text(
        text = label,
        color = colors.menuText,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

// ═══════════════ 工具 ═══════════════
private fun openAppInfo(context: Context, pkg: String) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:$pkg"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) {}
}

@Composable
private fun SearchIcon(color: Color, size: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.14f

        drawCircle(
            color = color,
            radius = w * 0.32f,
            center = Offset(w * 0.4f, h * 0.4f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )

        drawLine(
            color = color,
            start = Offset(w * 0.62f, h * 0.62f),
            end = Offset(w * 0.88f, h * 0.88f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun RefreshButton(
    isRefreshing: Boolean,
    colors: AppListColors,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "refreshRotation"
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.blockBg)
            .clickable(enabled = !isRefreshing, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(14.dp)
                .rotate(rotation)
        ) {
            val w = size.width
            val h = size.height
            val strokeWidth = w * 0.16f
            val color = colors.blockText

            // 画 3/4 圆弧
            drawArc(
                color = color,
                startAngle = 30f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(w - strokeWidth, h - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 箭头（在弧的起点画一个小三角）
            val arrowSize = w * 0.24f
            val cx = w / 2
            val cy = h / 2
            val r = w / 2 - strokeWidth / 2
            val angleRad = Math.toRadians(30.0)
            val ax = cx + r * kotlin.math.cos(angleRad).toFloat()
            val ay = cy + r * kotlin.math.sin(angleRad).toFloat()
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(ax - arrowSize / 2, ay + arrowSize / 2)
                lineTo(ax + arrowSize / 2, ay + arrowSize / 2)
                lineTo(ax, ay - arrowSize / 2)
                close()
            }
            drawPath(path, color)
        }
    }
}
// ═══════════════ 面板节点点击处理 ═══════════════
private fun handlePanelNodeClick(
    context: Context,
    id: String,
    openPicker: (key: String, title: String, intent: Intent) -> Unit
) {
    // 从 DataStore 同步读（简单起见用 runBlocking，通常很快）
    val prefs = context.getSharedPreferences("panel_dummy", Context.MODE_PRIVATE) // 占位

    when (id) {
        "home" -> { /* 只是关闭面板 */ }
        "team" -> { /* 无操作 */ }
        "terminal" -> { /* 无操作 */ }
        "shop" -> { /* 无操作 */ }
        "operator" -> {
            // 相册
            launchOrPick(
                context, "gallery", "选择相册应用",
                Intent(Intent.ACTION_VIEW).apply { type = "image/*" },
                openPicker
            )
        }
        "archive" -> {
            // 信息
            launchOrPick(
                context, "message", "选择信息应用",
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING),
                openPicker
            )
        }
        "base" -> {
            // 设置
            launchOrPick(
                context, "settings", "选择设置应用",
                Intent(Settings.ACTION_SETTINGS),
                openPicker
            )
        }
        "recruit" -> {
            // 浏览器
            launchOrPick(
                context, "browser", "选择浏览器",
                Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com")),
                openPicker
            )
        }
        "search" -> {
            // 应用商店
            launchOrPick(
                context, "appstore", "选择应用商店",
                Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=app")),
                openPicker
            )
        }
    }
}

private fun launchOrPick(
    context: Context,
    key: String,
    title: String,
    intent: Intent,
    openPicker: (String, String, Intent) -> Unit
) {
    // 从 DataStore 读缓存（用 runBlocking 同步读，因为只是读一个键）
    val pkg = kotlinx.coroutines.runBlocking {
        LauncherPreferences.getBoundAppFlow(context, key).firstOrNull()
    }
    if (!pkg.isNullOrEmpty()) {
        AppLauncher.launchPackage(context, pkg)
    } else {
        openPicker(key, title, intent)
    }
}

// ═══════════════ 返回/主页覆盖层 ═══════════════
@Composable
private fun BackHomeOverlay(
    onBack: (Offset) -> Unit,
    onBackCenterChanged: (Offset) -> Unit,
    onHome: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(174.dp)
                .height(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.topbar_left_active),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // ⭐ 主页按钮下方的白色辉光
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .width(104.dp)                 // 覆盖主页按钮区域（右 60%）
                    .height(10.dp)                 // 辉光高度
                    .offset(y = 10.dp)              // 往下溢出一点
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x88FFFFFF),  // 顶部白色
                                Color(0x33FFFFFF),  // 中间半透
                                Color(0x00FFFFFF)   // 底部透明
                            )
                        )
                    )
            )

            Row(Modifier.matchParentSize()) {
                var backBoxCenter by remember { mutableStateOf(Offset.Zero) }
                Box(
                    Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .onGloballyPositioned { coords ->
                            val bounds = coords.boundsInWindow()
                            backBoxCenter = Offset(
                                bounds.left + bounds.width / 2f,
                                bounds.top + bounds.height / 2f
                            )
                            onBackCenterChanged(backBoxCenter)
                        }
                        .clickable { onBack(backBoxCenter) }
                )
                Box(
                    Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .clickable { onHome() }
                )
            }
        }
        Spacer(Modifier.weight(1f))
    }
}