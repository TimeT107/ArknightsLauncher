package com.example.arklauncher.data

import android.content.Context
import android.os.BatteryManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LauncherStatusProvider {

    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    fun getStatus(context: Context): LauncherStatus {
        val now = LocalDateTime.now()
        val mode = dayModeOf(now.hour)
        return LauncherStatus(
            time = now.format(timeFmt),
            date = now.format(dateFmt),
            period = if (mode == DayMode.DAY) "☀ 白天" else "☾ 夜晚",
            battery = readBattery(context),
            dayMode = mode
        )
    }

    fun getDayMode(): DayMode = dayModeOf(LocalDateTime.now().hour)

    private fun dayModeOf(hour: Int): DayMode =
        if (hour in 6..17) DayMode.DAY else DayMode.NIGHT

    private fun readBattery(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            ?: return 0
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return if (level in 0..100) level else 0
    }
}