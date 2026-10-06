package com.example.arklauncher.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.arklauncher.data.AppInfo
import com.example.arklauncher.data.AppManager
import com.example.arklauncher.data.LauncherPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppPicker(
    slot: String,
    onSelect: (AppInfo) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val apps by produceState(
        initialValue = AppManager.getApps(context),
        context
    ) {
        if (value.isEmpty()) {
            value = withContext(Dispatchers.IO) {
                AppManager.refreshApps(context)
            }
        }
    }

    Column(modifier = Modifier.padding(20.dp)) {
        Text(text = "选择应用")
        Spacer(Modifier.height(20.dp))

        LazyColumn(modifier = Modifier.height(500.dp)) {
            items(apps, key = { it.packageName }) { app ->
                Text(
                    text = app.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable {
                            scope.launch {
                                try {
                                    LauncherPreferences.saveDockApp(
                                        context, slot, app.packageName
                                    )
                                } catch (e: Exception) {
                                    Log.e("AppPicker", "save failed", e)
                                } finally {
                                    onSelect(app)
                                }
                            }
                        }
                )
            }
        }
    }
}