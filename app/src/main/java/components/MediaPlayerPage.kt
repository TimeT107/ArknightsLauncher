package com.example.arklauncher.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arklauncher.data.MediaInfo
import com.example.arklauncher.data.MediaSessionHelper
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.interaction.MutableInteractionSource

private val MediaTextWhite = Color.White
private val MediaTextGray = Color(0x99FFFFFF)
private val MediaAccent = Color(0xFF4FC3F7)
private val MediaArtBg = Color(0xFF1A1A1A)
private val MediaTrackBg = Color(0x33FFFFFF)

@Composable
fun MediaPlayerPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var mediaInfo by remember { mutableStateOf<MediaInfo?>(null) }
    var hasPermission by remember { mutableStateOf(MediaSessionHelper.hasPermission(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            hasPermission = MediaSessionHelper.hasPermission(context)
            if (hasPermission) {
                mediaInfo = MediaSessionHelper.getCurrentMedia(context)
            }
            delay(1000L)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> NoPermissionHint(
                onGrant = { MediaSessionHelper.openNotificationAccessSettings(context) }
            )
            mediaInfo == null -> NoMediaHint()
            else -> MediaContent(
                info = mediaInfo!!,
                onToggle = { MediaSessionHelper.togglePlayPause(context) },
                onNext = { MediaSessionHelper.next(context) },
                onPrev = { MediaSessionHelper.previous(context) },
                onOpenApp = { MediaSessionHelper.openPlayerApp(context) }
            )
        }
    }
}

@Composable
private fun NoPermissionHint(onGrant: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .clickable(onClick = onGrant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "点击授予通知访问权限",
            fontSize = 11.sp,
            color = MediaTextGray
        )
    }
}

@Composable
private fun NoMediaHint() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("暂无播放", fontSize = 11.sp, color = MediaTextGray)
    }
}

@Composable
private fun MediaContent(
    info: MediaInfo,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpenApp: () -> Unit
) {
    val albumBitmap = remember(info.albumArt) {
        info.albumArt?.asImageBitmap()
    }

    // 外层 Box：点击空白区域触发展开
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = { com.example.arklauncher.data.MediaUiState.expand() }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面：点击打开 App
            Box(
                Modifier
                    .size(56.dp)
                    .background(MediaArtBg)
                    .clickable(onClick = onOpenApp),
                contentAlignment = Alignment.Center
            ) {
                if (albumBitmap != null) {
                    Image(
                        bitmap = albumBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("♪", fontSize = 26.sp, color = MediaTextWhite)
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = info.title,
                            fontSize = 13.sp,
                            color = MediaTextWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (info.artist.isNotEmpty()) {
                            Text(
                                text = info.artist,
                                fontSize = 10.sp,
                                color = MediaTextGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 控制按钮：点击时不会展开
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MediaIconButton(MediaIcon.Prev, onPrev)
                        MediaIconButton(
                            if (info.isPlaying) MediaIcon.Pause else MediaIcon.Play,
                            onToggle
                        )
                        MediaIconButton(MediaIcon.Next, onNext)
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val progress = if (info.duration > 0L) {
                        (info.position.toFloat() / info.duration.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp),
                        color = MediaAccent,
                        trackColor = MediaTrackBg
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = if (info.duration > 0L) formatTime(info.position) else "--:--",
                        fontSize = 9.sp,
                        color = MediaTextGray
                    )
                }
            }
        }
    }
}
private enum class MediaIcon { Play, Pause, Prev, Next }

@Composable
private fun MediaIconButton(icon: MediaIcon, onClick: () -> Unit) {
    Box(
        Modifier
            .size(22.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(13.dp)) {
            val w = size.width
            val h = size.height
            val white = Color.White

            when (icon) {
                MediaIcon.Play -> {
                    val path = Path().apply {
                        moveTo(w * 0.15f, 0f)
                        lineTo(w, h / 2)
                        lineTo(w * 0.15f, h)
                        close()
                    }
                    drawPath(path, white)
                }
                MediaIcon.Pause -> {
                    val barW = w * 0.3f
                    val gap = w * 0.15f
                    drawRect(
                        color = white,
                        topLeft = Offset(0f, 0f),
                        size = Size(barW, h)
                    )
                    drawRect(
                        color = white,
                        topLeft = Offset(barW + gap, 0f),
                        size = Size(barW, h)
                    )
                }
                MediaIcon.Prev -> {
                    // 左边竖条 + 指向左的三角形
                    val barW = w * 0.18f
                    drawRect(
                        color = white,
                        topLeft = Offset(0f, 0f),
                        size = Size(barW, h)
                    )
                    val path = Path().apply {
                        moveTo(w, 0f)
                        lineTo(barW + w * 0.1f, h / 2)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(path, white)
                }
                MediaIcon.Next -> {
                    // 指向右的三角形 + 右边竖条
                    val barW = w * 0.18f
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w - barW - w * 0.1f, h / 2)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path, white)
                    drawRect(
                        color = white,
                        topLeft = Offset(w - barW, 0f),
                        size = Size(barW, h)
                    )
                }
            }
        }
    }
}
private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}