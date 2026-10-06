package com.example.arklauncher.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arklauncher.data.MediaInfo
import com.example.arklauncher.data.MediaSessionHelper
import com.example.arklauncher.data.MediaUiState
import kotlinx.coroutines.delay

private val MediaPanelBg = Color(0xF0181818)
private val MediaTextWhite = Color.White
private val MediaTextGray = Color(0x99FFFFFF)
private val MediaAccent = Color(0xFF4FC3F7)
private val MediaTrackBg = Color(0x33FFFFFF)

@Composable
fun MediaExpandedPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var mediaInfo by remember { mutableStateOf<MediaInfo?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            mediaInfo = MediaSessionHelper.getCurrentMedia(context)
            delay(500L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(4.dp, RectangleShape, clip = false)
            .background(MediaPanelBg)
    ) {
        val info = mediaInfo
        if (info == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无播放", fontSize = 14.sp, color = MediaTextGray)
            }
        } else {
            ExpandedContent(info = info)
        }

        // 右上角关闭按钮
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 12.dp)
                .size(28.dp)
                .clickable { MediaUiState.collapse() },
            contentAlignment = Alignment.Center
        ) {
            Text("✕", fontSize = 16.sp, color = MediaTextGray)
        }
    }
}

@Composable
private fun ExpandedContent(info: MediaInfo) {
    val context = LocalContext.current
    val albumBitmap = remember(info.albumArt) { info.albumArt?.asImageBitmap() }

    Box(Modifier.fillMaxSize()) {

        // ─── 背景：专辑封面（模糊/暗化）───
        if (albumBitmap != null) {
            Image(
                bitmap = albumBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // 半透明黑遮罩
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 14.dp)
        ) {
            // ─── 顶部：音符图标 ───
            Text("♪", fontSize = 22.sp, color = MediaAccent)

            Spacer(Modifier.weight(1f))

            // ─── 中部：歌名 + 歌手 + 右侧大播放按钮 ───
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = info.artist.ifEmpty { "未知歌手" },
                        fontSize = 14.sp,
                        color = MediaTextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = info.title,
                        fontSize = 24.sp,
                        color = MediaTextWhite,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(16.dp))

                // 大播放按钮
                Box(
                    Modifier
                        .size(56.dp)
                        .background(MediaAccent)
                        .clickable { MediaSessionHelper.togglePlayPause(context) },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        val w = size.width
                        val h = size.height
                        val dark = Color(0xFF141414)
                        if (info.isPlaying) {
                            val barW = w * 0.3f
                            val gap = w * 0.15f
                            drawRect(dark, Offset(0f, 0f), Size(barW, h))
                            drawRect(dark, Offset(barW + gap, 0f), Size(barW, h))
                        } else {
                            val path = Path().apply {
                                moveTo(w * 0.15f, 0f)
                                lineTo(w, h / 2)
                                lineTo(w * 0.15f, h)
                                close()
                            }
                            drawPath(path, dark)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ─── 底部：上一首 + 进度条 + 下一首 ───
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpandedIconButton(ExpandedIcon.Prev) {
                    MediaSessionHelper.previous(context)
                }

                Spacer(Modifier.width(12.dp))

                val progress = if (info.duration > 0L) {
                    (info.position.toFloat() / info.duration.toFloat()).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp),
                    color = MediaTextWhite,
                    trackColor = MediaTrackBg
                )

                Spacer(Modifier.width(12.dp))

                ExpandedIconButton(ExpandedIcon.Next) {
                    MediaSessionHelper.next(context)
                }
            }
        }
    }
}

private enum class ExpandedIcon { Play, Pause, Prev, Next }

@Composable
private fun ExpandedIconButton(icon: ExpandedIcon, onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            val white = Color.White

            when (icon) {
                ExpandedIcon.Play -> {
                    val path = Path().apply {
                        moveTo(w * 0.15f, 0f)
                        lineTo(w, h / 2)
                        lineTo(w * 0.15f, h)
                        close()
                    }
                    drawPath(path, white)
                }
                ExpandedIcon.Pause -> {
                    val barW = w * 0.3f
                    val gap = w * 0.15f
                    drawRect(white, Offset(0f, 0f), Size(barW, h))
                    drawRect(white, Offset(barW + gap, 0f), Size(barW, h))
                }
                ExpandedIcon.Prev -> {
                    val barW = w * 0.18f
                    drawRect(white, Offset(0f, 0f), Size(barW, h))
                    val path = Path().apply {
                        moveTo(w, 0f)
                        lineTo(barW + w * 0.1f, h / 2)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(path, white)
                }
                ExpandedIcon.Next -> {
                    val barW = w * 0.18f
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w - barW - w * 0.1f, h / 2)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path, white)
                    drawRect(white, Offset(w - barW, 0f), Size(barW, h))
                }
            }
        }
    }
}