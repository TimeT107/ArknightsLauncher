package com.example.arklauncher.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val firstInstallTime: Long = 0L,
    val lastUsedTime: Long = 0L,      // 新增
    val usageSeconds: Long = 0L       // 新增：累计前台时长
)