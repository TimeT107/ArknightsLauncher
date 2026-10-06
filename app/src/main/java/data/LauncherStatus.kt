package com.example.arklauncher.data

data class LauncherStatus(
    val time: String,
    val date: String,
    val period: String,
    val battery: Int,
    val dayMode: DayMode
)