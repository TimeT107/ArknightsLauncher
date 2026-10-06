package com.example.arklauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arklauncher.components.AppFont
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.aspectRatio

private data class NodeSpec(
    val id: String,
    val label: String,
    val relX: Float,
    val relY: Float,
    val circleSize: Dp,
    val icon: String
)

private val Nodes = emptyList<NodeSpec>()

@Composable
fun HomePanel(
    modifier: Modifier = Modifier,
    onNodeClick: (String) -> Unit
) {
    val config = LocalConfiguration.current
    val panelHeightDp = (config.screenHeightDp * 0.25f).dp
    val panelWidthDp = config.screenWidthDp.dp
    val panelHeightPx = with(LocalDensity.current) { panelHeightDp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(panelHeightDp)
    ) {
        // ⭐ 背景：从上到下黑 → 半透明
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF000000),
                            Color(0xDD000000),
                            Color(0x88000000)
                        ),
                        startY = 0f,
                        endY = panelHeightPx
                    )
                )
        )

        // 星座连线
        Canvas(Modifier.fillMaxSize()) {
            val pts = Nodes.map { spec ->
                Offset(
                    x = size.width * spec.relX,
                    y = size.height * spec.relY
                )
            }
            val connections = emptyList<Pair<Int, Int>>()
            connections.forEach { (a, b) ->
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = pts[a],
                    end = pts[b],
                    strokeWidth = 1.2f
                )
            }
        }

        // ⭐ 首页：用图片替换（含圆圈 + 连线 + 图标）
        Box(
            modifier = Modifier
                .offset(x = panelWidthDp * 0.08f - 32.dp, y = panelHeightDp * 0.30f)
                .size(width = 64.dp, height = 100.dp)
                .clickable { onNodeClick("home") }
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = com.example.arklauncher.R.drawable.home_node
                ),
                contentDescription = "首页",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // ⭐ 编队：用图片替换
        Box(
            modifier = Modifier
                .offset(x = panelWidthDp * 0.24f - 24.dp, y = panelHeightDp * 0.20f)
                .size(width = 48.dp, height = 75.dp)
                .clickable { onNodeClick("team") }
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = com.example.arklauncher.R.drawable.team_node
                ),
                contentDescription = "编队",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // ⭐ 干员：用图片替换
        Box(
            modifier = Modifier
                .offset(x = panelWidthDp * 0.32f - 24.dp, y = panelHeightDp * 0.55f)
                .size(width = 48.dp, height = 75.dp)
                .clickable { onNodeClick("operator") }
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = com.example.arklauncher.R.drawable.operator_node
                ),
                contentDescription = "干员",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // ⭐ 档案：用图片替换
        Box(
            modifier = Modifier
                .offset(x = panelWidthDp * 0.46f - 24.dp, y = panelHeightDp * 0.55f)
                .size(width = 48.dp, height = 75.dp)
                .clickable { onNodeClick("archive") }
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = com.example.arklauncher.R.drawable.archive_node
                ),
                contentDescription = "档案",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // ⭐ 终端：用图片替换
        Box(
            modifier = Modifier
                .offset(x = panelWidthDp * 0.52f - 24.dp, y = panelHeightDp * 0.18f)
                .size(width = 48.dp, height = 80.dp)
                .clickable { onNodeClick("terminal") }
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = com.example.arklauncher.R.drawable.terminal_node
                ),
                contentDescription = "终端",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // ⭐ 基建：用图片替换
        Box(
            modifier = Modifier
                .offset(x = panelWidthDp * 0.68f - 24.dp, y = panelHeightDp * 0.18f)
                .size(width = 48.dp, height = 80.dp)
                .clickable { onNodeClick("base") }
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = com.example.arklauncher.R.drawable.base_node
                ),
                contentDescription = "基建",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // ⭐ 公开招募 / 干员寻访 / 采购中心：用一张图替换
        Box(
            modifier = Modifier
                .offset(x = panelWidthDp * 0.6f, y = panelHeightDp * 0.6f)
                .width(panelWidthDp * 0.45f)
                .aspectRatio(2.2f)          // 图片宽高比 ≈ 2.2:1
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = com.example.arklauncher.R.drawable.recruit_nodes
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // ⭐ 三段点击区，对应图中三个图标
            Row(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .weight(0.32f)
                        .fillMaxHeight()
                        .clickable { onNodeClick("recruit") }
                )
                Box(
                    Modifier
                        .weight(0.30f)
                        .fillMaxHeight()
                        .clickable { onNodeClick("search") }
                )
                Box(
                    Modifier
                        .weight(0.38f)
                        .fillMaxHeight()
                        .clickable { onNodeClick("shop") }
                )
            }
        }

        // 节点
        Nodes.forEach { spec ->
            PanelNode(
                spec = spec,
                parentW = panelWidthDp,
                parentH = panelHeightDp,
                onClick = { onNodeClick(spec.id) }
            )
        }

        // ⭐ 底部分界线
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0x99FFFFFF))
        )
    }
}

@Composable
private fun PanelNode(
    spec: NodeSpec,
    parentW: Dp,
    parentH: Dp,
    onClick: () -> Unit
) {
    val x = parentW * spec.relX
    val y = parentH * spec.relY
    val halfCircle = spec.circleSize / 2

    Column(
        modifier = Modifier
            .offset(x = x - halfCircle, y = y - halfCircle)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(spec.circleSize)
                .border(1.2.dp, Color(0xAAFFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = spec.icon,
                fontSize = (spec.circleSize.value * 0.45f).sp,
                color = Color.White
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = spec.label,
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = AppFont.hkstw12
        )
    }
}