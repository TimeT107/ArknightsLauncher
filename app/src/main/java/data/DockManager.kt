package com.example.arklauncher.data

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object DockManager {

    fun getDockApp(context: Context, packageName: String?): DockApp? {
        if (packageName.isNullOrEmpty()) return null
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            DockApp(
                packageName = packageName,
                name = pm.getApplicationLabel(info).toString(),
                icon = pm.getApplicationIcon(info)
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        } catch (e: Exception) {
            Log.w("DockManager", "getDockApp failed: $packageName", e)
            null
        }
    }
}