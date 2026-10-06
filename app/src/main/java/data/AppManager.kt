package com.example.arklauncher.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.text.Collator
import java.util.Locale

object AppManager {

    @Volatile
    private var cachedApps: List<AppInfo>? = null

    @Volatile
    private var cachedUsageApps: List<AppInfo>? = null

    fun getApps(context: Context): List<AppInfo> {
        cachedApps?.let { return it }
        return refreshApps(context)
    }

    fun refreshApps(context: Context): List<AppInfo> {
        val apps = queryApps(context)
        cachedApps = apps
        return apps
    }

    /**
     * 带使用数据（最近使用 / 累计时长）的应用列表。
     * 有缓存直接返回，避免每次重扫。
     */
    fun getAppsWithUsage(context: Context): List<AppInfo> {
        cachedUsageApps?.let { return it }
        val result = buildUsageApps(context)
        cachedUsageApps = result
        return result
    }

    /** 强制刷新 usage 缓存（下拉刷新时用）。 */
    fun refreshAppsWithUsage(context: Context): List<AppInfo> {
        // 清掉两个缓存，强制重扫
        cachedApps = null
        cachedUsageApps = null
        return getAppsWithUsage(context)
    }

    /** 供 UI 层直接读缓存（首帧避免空列表）。 */
    fun getCachedUsageApps(): List<AppInfo>? = cachedUsageApps

    private fun buildUsageApps(context: Context): List<AppInfo> {
        val base = getApps(context)
        if (!RecentAppTracker.hasPermission(context)) return base

        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val begin = end - 1000L * 60 * 60 * 24 * 30

            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, begin, end)

            val lastMap = HashMap<String, Long>()
            val timeMap = HashMap<String, Long>()
            stats?.forEach { s ->
                val last = lastMap[s.packageName] ?: 0L
                if (s.lastTimeUsed > last) lastMap[s.packageName] = s.lastTimeUsed
                timeMap[s.packageName] = (timeMap[s.packageName] ?: 0L) + s.totalTimeInForeground
            }

            base.map { app ->
                app.copy(
                    lastUsedTime = lastMap[app.packageName] ?: 0L,
                    usageSeconds = (timeMap[app.packageName] ?: 0L) / 1000
                )
            }
        } catch (e: Exception) {
            Log.e("AppManager", "buildUsageApps failed", e)
            base
        }
    }

    private fun queryApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val collator = Collator.getInstance(Locale.CHINA)
        return try {
            pm.getInstalledApplications(0)
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map {
                    val pkgInfo = try {
                        pm.getPackageInfo(it.packageName, 0)
                    } catch (_: Exception) {
                        null
                    }
                    AppInfo(
                        name = pm.getApplicationLabel(it).toString(),
                        packageName = it.packageName,
                        icon = pm.getApplicationIcon(it),
                        firstInstallTime = pkgInfo?.firstInstallTime ?: 0L
                    )
                }
                .sortedWith { a, b -> collator.compare(a.name, b.name) }
        } catch (e: Exception) {
            Log.e("AppManager", "queryApps failed", e)
            emptyList()
        }
    }

    fun getAppsForIntent(context: Context, intent: android.content.Intent): List<AppInfo> {
        val pm = context.packageManager
        return try {
            pm.queryIntentActivities(intent, 0)
                .mapNotNull { resolveInfo ->
                    try {
                        val pkgName = resolveInfo.activityInfo.packageName
                        val appInfo = pm.getApplicationInfo(pkgName, 0)
                        AppInfo(
                            name = pm.getApplicationLabel(appInfo).toString(),
                            packageName = pkgName,
                            icon = pm.getApplicationIcon(appInfo),
                            firstInstallTime = 0L
                        )
                    } catch (_: Exception) { null }
                }
                .distinctBy { it.packageName }
                .sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            Log.e("AppManager", "getAppsForIntent failed", e)
            emptyList()
        }
    }

    fun launchApp(context: Context, packageName: String) {
        AppLauncher.launchPackage(context, packageName)
    }
}