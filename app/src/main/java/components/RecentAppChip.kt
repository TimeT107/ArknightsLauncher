package com.example.arklauncher.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.arklauncher.data.AppInfo
import com.example.arklauncher.data.AppLauncher
import com.example.arklauncher.data.DockManager
import com.example.arklauncher.data.RecentAppTracker
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun RecentAppChip(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var recentApp by remember { mutableStateOf<AppInfo?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val pkg = RecentAppTracker.getLastApp(context)
                recentApp = if (pkg != null) {
                    DockManager.getDockApp(context, pkg)?.let {
                        AppInfo(it.name, it.packageName, it.icon, 0L)
                    }
                } else null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val app = recentApp ?: return

    Column(
        modifier = modifier
            .clickable { AppLauncher.launchPackage(context, app.packageName) }
            .padding(start = 4.dp, end = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberDrawablePainter(app.icon),
            contentDescription = app.name,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = app.name,
            fontSize = 10.sp,
            color = Color(0xFF141414),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 56.dp)
        )
    }
}