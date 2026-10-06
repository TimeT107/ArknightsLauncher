@file:OptIn(ExperimentalFoundationApi::class)

package com.example.arklauncher.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arklauncher.R
import com.example.arklauncher.data.AppInfo
import com.example.arklauncher.data.AppLauncher
import com.example.arklauncher.data.AppManager
import com.example.arklauncher.data.DayMode
import com.example.arklauncher.data.LauncherPreferences
import com.example.arklauncher.data.LauncherState
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.runtime.mutableStateOf



// ═══════════════ 配色方案 ═══════════════
data class TerminalColors(
    val panelBg: Color,
    val panelBgPressed: Color,
    val textDark: Color,
    val textGray: Color,
    val textWhite: Color,
    val shopBg: Color,
    val accentBar: Color,
    val warehouseBg: Color,
    val warehouseText: Color,
    val batteryBlockBg: Color,
    val batteryBarBg: Color,
    val batteryNumColor: Color,
    val batteryLabelColor: Color
)

private val DayColors = TerminalColors(
    panelBg = Color(0xE6FFFFFF),
    panelBgPressed = Color(0xFFB0B0B0),
    textDark = Color(0xFF141414),
    textGray = Color(0xFF6E6E6E),
    textWhite = Color.White,
    shopBg = Color(0xFF1FB4F0),
    accentBar = Color(0xFFFF6B1A),
    warehouseBg = Color(0xFF3A3A3A),
    warehouseText = Color.White,
    batteryBlockBg = Color(0xCCD0D0D0),
    batteryBarBg = Color(0xCC1A1A1A),
    batteryNumColor = Color(0xFF141414),
    batteryLabelColor = Color.White
)

private val NightColors = TerminalColors(
    panelBg = Color(0xE61F1F1F),
    panelBgPressed = Color(0xFF4A4A4A),
    textDark = Color.White,
    textGray = Color(0xFFAAAAAA),
    textWhite = Color.White,
    shopBg = Color(0xFF0E7FBA),
    accentBar = Color(0xFFFF6B1A),
    warehouseBg = Color(0xFF141414),
    warehouseText = Color.White,
    batteryBlockBg = Color(0xCC3A3A3A),
    batteryBarBg = Color(0xCC0A0A0A),
    batteryNumColor = Color.White,
    batteryLabelColor = Color.White
)

private fun terminalColorsFor(dayMode: DayMode): TerminalColors =
    if (dayMode == DayMode.DAY) DayColors else NightColors

// ═══════════════ 阴影扩展 ═══════════════
private fun Modifier.panelShadow(elevation: Dp = 4.dp): Modifier = this.shadow(
    elevation = elevation,
    shape = RectangleShape,
    clip = false
)

// ═══════════════ 绑定键 ═══════════════
private const val KEY_GALLERY = "gallery"
private const val KEY_BROWSER = "browser"
private const val KEY_APPSTORE = "appstore"
private const val KEY_CALENDAR = "calendar"
private const val KEY_SETTINGS = "settings"
private const val KEY_FILES = "files"

@Composable
fun ArkTerminal(
    modifier: Modifier = Modifier,
    onTerminalClick: (Offset) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val battery = LauncherState.status.value.battery
    val colors = terminalColorsFor(LauncherState.status.value.dayMode)

    val galleryPkg by rememberBoundPkg(context, KEY_GALLERY)
    val browserPkg by rememberBoundPkg(context, KEY_BROWSER)
    val appstorePkg by rememberBoundPkg(context, KEY_APPSTORE)
    val calendarPkg by rememberBoundPkg(context, KEY_CALENDAR)
    val settingsPkg by rememberBoundPkg(context, KEY_SETTINGS)
    val filesPkg by rememberBoundPkg(context, KEY_FILES)

    var pickerKey by remember { mutableStateOf<String?>(null) }
    var pickerTitle by remember { mutableStateOf("") }
    var pickerIntent by remember { mutableStateOf<Intent?>(null) }

    fun launchOrPick(key: String, title: String, boundPkg: String?, intent: Intent) {
        if (!boundPkg.isNullOrEmpty()) {
            AppLauncher.launchPackage(context, boundPkg)
        } else {
            pickerKey = key
            pickerTitle = title
            pickerIntent = intent
        }
    }

    val browserIntent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com")) }
    val appstoreIntent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=app")) }
    val calendarIntent = remember { Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR) }
    val settingsIntent = remember { Intent(Settings.ACTION_SETTINGS) }
    val filesIntent = remember { Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_FILES) }
    val galleryIntent = remember { Intent(Intent.ACTION_VIEW).apply { type = "image/*" } }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End
    ) {
        TerminalMainPanel(
            battery = battery,
            colors = colors,
            onClick = onTerminalClick,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TerminalCard(
                modifier = Modifier.weight(1f),
                title = "编队",
                subtitle = "角色管理",
                background = colors.panelBg,
                titleColor = colors.textDark,
                subtitleColor = colors.textGray,
                decoRes = R.drawable.team_castle,
                decoSize = 90.dp,
                decoEndPadding = 0.dp
            )
            TerminalCard(
                modifier = Modifier.weight(1f),
                title = "干员",
                subtitle = "角色管理",
                background = colors.panelBg,
                titleColor = colors.textDark,
                subtitleColor = colors.textGray,
                decoRes = R.drawable.operator_deco,
                decoSize = 90.dp,
                decoEndPadding = 0.dp,
                onClick = {
                    launchOrPick(KEY_GALLERY, "选择相册应用", galleryPkg, galleryIntent)
                },
                onLongClick = {
                    pickerKey = KEY_GALLERY
                    pickerTitle = "重新选择相册应用"
                    pickerIntent = galleryIntent
                }
            )
        }

        TerminalDrawerPanel(
            modifier = Modifier.fillMaxWidth(0.92f),
            colors = colors,
            onPublicRecruit = {
                launchOrPick(KEY_BROWSER, "选择浏览器", browserPkg, browserIntent)
            },
            onOperatorSearch = {
                launchOrPick(KEY_APPSTORE, "选择应用商店", appstorePkg, appstoreIntent)
            },
            onPublicRecruitLong = {
                pickerKey = KEY_BROWSER
                pickerTitle = "重新选择浏览器"
                pickerIntent = browserIntent
            },
            onOperatorSearchLong = {
                pickerKey = KEY_APPSTORE
                pickerTitle = "重新选择应用商店"
                pickerIntent = appstoreIntent
            }
        )

        Row(
            Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TerminalCard(
                modifier = Modifier.fillMaxWidth(),
                title = "任务",
                subtitle = "",
                background = colors.panelBg,
                titleColor = colors.textDark,
                subtitleColor = colors.textGray,
                decoRes = R.drawable.mission_deco,
                decoSize = 90.dp,
                decoEndPadding = 0.dp,
                topAccent = true,
                onClick = {
                    launchOrPick(KEY_CALENDAR, "选择日历应用", calendarPkg, calendarIntent)
                },
                onLongClick = {
                    pickerKey = KEY_CALENDAR
                    pickerTitle = "重新选择日历应用"
                    pickerIntent = calendarIntent
                }
            )
        }

        Row(
            Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TerminalCard(
                modifier = Modifier.weight(1f),
                title = "基建",
                subtitle = "",
                background = colors.panelBg,
                titleColor = colors.textDark,
                subtitleColor = colors.textGray,
                decoRes = R.drawable.base_deco,
                decoSize = 90.dp,
                decoEndPadding = 0.dp,
                onClick = {
                    launchOrPick(KEY_SETTINGS, "选择设置应用", settingsPkg, settingsIntent)
                },
                onLongClick = {
                    pickerKey = KEY_SETTINGS
                    pickerTitle = "重新选择设置应用"
                    pickerIntent = settingsIntent
                }
            )
            WarehouseCard(
                modifier = Modifier.weight(0.7f),
                colors = colors,
                onClick = {
                    launchOrPick(KEY_FILES, "选择文件管理应用", filesPkg, filesIntent)
                },
                onLongClick = {
                    pickerKey = KEY_FILES
                    pickerTitle = "重新选择文件管理应用"
                    pickerIntent = filesIntent
                }
            )
        }
    }

    if (pickerKey != null && pickerIntent != null) {
        AppPickerDialog(
            title = pickerTitle,
            intent = pickerIntent!!,
            onDismiss = { pickerKey = null; pickerIntent = null },
            onSelect = { app ->
                val key = pickerKey!!
                scope.launch {
                    LauncherPreferences.saveBoundApp(context, key, app.packageName)
                }
                pickerKey = null
                pickerIntent = null
                AppLauncher.launchPackage(context, app.packageName)
            }
        )
    }
}

@Composable
private fun rememberBoundPkg(context: android.content.Context, key: String): State<String?> {
    val flow = remember(context, key) { LauncherPreferences.getBoundAppFlow(context, key) }
    return flow.collectAsState(initial = null)
}

@Composable
fun AppPickerDialog(
    title: String,
    intent: Intent,
    onDismiss: () -> Unit,
    onSelect: (AppInfo) -> Unit
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }

    LaunchedEffect(intent) {
        apps = withContext(Dispatchers.IO) {
            AppManager.getAppsForIntent(context, intent)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (apps.isEmpty()) {
                Text("没有找到可用的应用")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(apps, key = { it.packageName }) { app ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(app) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberDrawablePainter(app.icon),
                                contentDescription = app.name,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(app.name, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun TerminalMainPanel(
    battery: Int,
    colors: TerminalColors,
    onClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bg = if (pressed) colors.panelBgPressed else colors.panelBg

    var panelCenter by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    val bounds = coords.boundsInWindow()
                    panelCenter = Offset(
                        bounds.left + bounds.width / 2f,
                        bounds.top + bounds.height / 2f
                    )
                }
                .panelShadow()
                .background(bg)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onClick(panelCenter) }
                )
        )

        // ─── 第 2 层（中）：装饰图片 ───
        Image(
            painter = painterResource(id = R.drawable.terminal_deco),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 30.dp)
                .alpha(if (pressed) 0f else 1f)
        )

        // ─── 第 3 层（最上）：文字 + 内容（含 RecentAppChip）───
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 电量板块
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(95.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(colors.batteryBlockBg)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(colors.batteryBarBg)
                            .height(16.dp)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.power_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.BottomCenter)
                        .offset(x = -8.dp, y = -6.dp)
                )

                Text(
                    text = "$battery",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = colors.batteryNumColor,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0x66000000),
                            offset = Offset(6f, 7f),
                            blurRadius = 1f
                        )
                    ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 5.dp, y = -4.dp)
                        .padding(top = 6.dp)
                )

                Text(
                    text = "电量 / 100",
                    fontSize = 10.sp,
                    color = colors.batteryLabelColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // 终端 + 当前 + 全部完成
            Column {
                Text(
                    text = "终端",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textDark,
                    fontFamily = AppFont.hkstw12
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2B2B2B), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "当前",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "全部完成",
                    fontSize = 7.sp,
                    color = colors.textGray
                )
            }

            Spacer(Modifier.weight(1f))

            // RecentAppChip 最上层，会压住装饰图片
            RecentAppChip()
        }
        // ⭐ 橙色横条：底部，从电量板块右边缘延伸到面板右边
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 145.dp, end = 1.dp)
                .height(3.dp)
                .background(Color(0xFFFF6B1A))
        )
    }
}

@Composable
fun TerminalCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    background: Color,
    titleColor: Color,
    subtitleColor: Color,
    big: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    decoRes: Int? = null,
    decoSize: Dp = 52.dp,
    decoEndPadding: Dp = 8.dp,
    topAccent: Boolean = false
) {
    Box(
        modifier = modifier
            .height(if (big) 84.dp else 64.dp)
            .panelShadow()
            .background(background)
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
    ) {
        // 顶部橙条
        if (topAccent) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.62f)
                    .height(3.dp)
                    .background(Color(0xFFFF6B1A))
            )
        }

        // 装饰图
        if (decoRes != null) {
            Image(
                painter = painterResource(id = decoRes),
                contentDescription = null,
                modifier = Modifier
                    .size(decoSize)
                    .align(Alignment.CenterEnd)
                    .padding(end = decoEndPadding)
            )
        }

        // 文字
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = if (big) 26.sp else 22.sp,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                fontFamily = AppFont.hkstw12
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = subtitleColor,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TerminalDrawerPanel(
    modifier: Modifier = Modifier,
    colors: TerminalColors,
    onPublicRecruit: () -> Unit = {},
    onOperatorSearch: () -> Unit = {},
    onPublicRecruitLong: (() -> Unit)? = null,
    onOperatorSearchLong: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val rightShrink by animateDpAsState(
        targetValue = if (expanded) 150.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "drawerShrink"
    )

    Box(modifier = modifier.height(84.dp)) {

        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                Modifier
                    .width(150.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .panelShadow(3.dp)
                        .background(colors.shopBg)
                        .then(
                            if (onPublicRecruitLong != null) {
                                Modifier.combinedClickable(
                                    onClick = onPublicRecruit,
                                    onLongClick = onPublicRecruitLong
                                )
                            } else {
                                Modifier.clickable(onClick = onPublicRecruit)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "公开招募",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .panelShadow(3.dp)
                        .background(colors.shopBg)
                        .then(
                            if (onOperatorSearchLong != null) {
                                Modifier.combinedClickable(
                                    onClick = onOperatorSearch,
                                    onLongClick = onOperatorSearchLong
                                )
                            } else {
                                Modifier.clickable(onClick = onOperatorSearch)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "干员寻访",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(end = rightShrink)
                .panelShadow()
                .background(colors.shopBg)
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.shop_deco),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = 2.0f
                        scaleY = 1.5f
                    }
                    .align(Alignment.CenterEnd)
                    .padding(end = 30.dp)
            )
            Text(
                "采购中心",
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = AppFont.hkstw12,
                modifier = Modifier.padding(start = 18.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WarehouseCard(
    modifier: Modifier = Modifier,
    colors: TerminalColors,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .panelShadow()
            .background(colors.warehouseBg)
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.warehouse_deco),
            contentDescription = null,
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp)
        )
        Text(
            text = "仓库",
            fontSize = 20.sp,
            color = colors.warehouseText,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFont.hkstw12,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 10.dp)
        )
    }
}