package com.example.arklauncher.data

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.View

object AppLauncher {

    /**
     * 普通启动（无动画）
     */
    fun launchPackage(context: Context, packageName: String?) {
        if (packageName == null) return
        try {
            val intent = context.packageManager
                .getLaunchIntentForPackage(packageName) ?: return
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w("AppLauncher", "launch failed: $packageName", e)
        }
    }

    /**
     * 从指定矩形区域放大启动应用（Pixel 桌面点击图标的效果）。
     *
     * @param sourceView 触发点击的 View（用 LocalView.current 拿）
     * @param bounds 图标在屏幕上的矩形区域（用 boundsInScreen() 拿）
     */
    fun launchPackageWithScaleUp(
        context: Context,
        packageName: String?,
        sourceView: View?,
        bounds: Rect?
    ) {
        if (packageName == null) return
        try {
            val intent = context.packageManager
                .getLaunchIntentForPackage(packageName) ?: return

            val options = if (sourceView != null && bounds != null && !bounds.isEmpty) {
                ActivityOptions.makeScaleUpAnimation(
                    sourceView,
                    bounds.left,
                    bounds.top,
                    bounds.width(),
                    bounds.height()
                ).toBundle()
            } else if (sourceView != null) {
                ActivityOptions.makeScaleUpAnimation(
                    sourceView,
                    0, 0,
                    sourceView.width,
                    sourceView.height
                ).toBundle()
            } else null

            if (options != null) {
                context.startActivity(intent, options)
            } else {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.w("AppLauncher", "launch with scale up failed: $packageName", e)
            launchPackage(context, packageName)
        }
    }
}