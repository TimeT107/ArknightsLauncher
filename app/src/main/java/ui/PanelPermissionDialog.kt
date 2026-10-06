package com.example.arklauncher.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val serviceId = "${context.packageName}/${context.packageName}.service.PanelAccessibilityService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.split(":").any { it.equals(serviceId, ignoreCase = true) }
}

fun openAccessibilitySettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) {}
}

@Composable
fun PanelPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("开启下滑呼出面板", fontSize = 16.sp) },
        text = {
            Text(
                text = """
                    要使用下滑呼出通知中心/控制中心，需要你开启"无障碍"权限。

                    步骤：
                    1. 点击下方"前往设置"
                    2. 在列表中找到「Arknights Launcher 面板」
                    3. 打开开关
                    4. 返回桌面即可使用
                """.trimIndent(),
                fontSize = 13.sp
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
                openAccessibilitySettings(context)
            }) {
                Text("前往设置", color = Color(0xFF4FC3F7))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("以后再说")
            }
        }
    )
}