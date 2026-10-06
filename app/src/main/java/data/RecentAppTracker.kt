package com.example.arklauncher.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent

object RecentAppTracker {

    fun getLastApp(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val end = System.currentTimeMillis()
        val begin = end - 1000L * 60 * 60 * 24   // 过去 24 小时

        val launcherPkgs = getLauncherPackages(context)

        val events = usm.queryEvents(begin, end)
        var lastPkg: String? = null
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                val pkg = event.packageName
                if (pkg != null
                    && pkg != context.packageName
                    && pkg !in launcherPkgs
                ) {
                    lastPkg = pkg
                }
            }
        }
        return lastPkg
    }

    /** 当前设备上所有 HOME 类应用的包名。 */
    private fun getLauncherPackages(context: Context): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        return context.packageManager
            .queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }
            .toSet()
    }

    fun hasPermission(context: Context): Boolean {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return false
        val end = System.currentTimeMillis()
        val begin = end - 1000L * 60 * 60
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, begin, end)
        return !stats.isNullOrEmpty()
    }
}