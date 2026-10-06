package com.example.arklauncher.data

data class SystemStatus(
    val cpuPercent: Int = 0,
    val memPercent: Int = 0,
    val diskPercent: Int = 0,
    val batteryPercent: Int = 0
)