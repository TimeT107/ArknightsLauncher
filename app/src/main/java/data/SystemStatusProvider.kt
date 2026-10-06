package com.example.arklauncher.data

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import java.io.File

object SystemStatusProvider {

    fun getStatus(context: Context): SystemStatus = SystemStatus(
        cpuPercent = readCpuPercent(),
        memPercent = readMemPercent(context),
        diskPercent = readDiskPercent(),
        batteryPercent = readBatteryPercent(context)
    )

    /**
     * 用"当前频率 / 最大频率"近似 CPU 占用率。
     * /proc/stat 在 Android 10+ 被 SELinux 禁止，只能读 cpufreq。
     */
    private fun readCpuPercent(): Int = try {
        var sumCur = 0L
        var sumMax = 0L
        val coreCount = Runtime.getRuntime().availableProcessors()
        for (i in 0 until coreCount) {
            val cur = readLongFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
            val max = readLongFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq")
            if (cur > 0 && max > 0) {
                sumCur += cur
                sumMax += max
            }
        }
        if (sumMax > 0) ((sumCur * 100L) / sumMax).toInt().coerceIn(0, 100) else 0
    } catch (_: Exception) { 0 }

    private fun readLongFile(path: String): Long = try {
        File(path).readText().trim().toLongOrNull() ?: 0L
    } catch (_: Exception) { 0L }

    private fun readMemPercent(context: Context): Int = try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val used = mi.totalMem - mi.availMem
        ((used * 100L) / mi.totalMem).toInt().coerceIn(0, 100)
    } catch (_: Exception) { 0 }

    private fun readDiskPercent(): Int = try {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val avail = stat.availableBlocksLong * stat.blockSizeLong
        if (total <= 0) 0
        else (((total - avail) * 100L) / total).toInt().coerceIn(0, 100)
    } catch (_: Exception) { 0 }

    private fun readBatteryPercent(context: Context): Int = try {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
    } catch (_: Exception) { 0 }
}