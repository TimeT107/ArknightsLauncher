package com.example.arklauncher.data

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log

object TorchState {
    var isOn: Boolean = false
}

object QuickToggles {

    // ═════ WiFi ═════
    fun isWifiOn(context: Context): Boolean = try {
        (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled
    } catch (_: Exception) { false }

    fun toggleWifi(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openWifiPanel(context)
        } else {
            try {
                @Suppress("DEPRECATION")
                val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                @Suppress("DEPRECATION")
                wm.isWifiEnabled = !wm.isWifiEnabled
            } catch (_: Exception) { openWifiSettings(context) }
        }
    }

    private fun openWifiPanel(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) { openWifiSettings(context) }
    }

    private fun openWifiSettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {}
    }

    // ═════ Bluetooth ═════
    fun isBluetoothOn(context: Context): Boolean = try {
        val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bm.adapter?.isEnabled == true
    } catch (_: Exception) { false }

    fun toggleBluetooth(context: Context) {
        // Android 12+ 需要 BLUETOOTH_CONNECT 运行时权限，直接跳设置页
        try {
            context.startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Log.w("QuickToggles", "bluetooth settings failed", e)
        }
    }

    // ═════ Torch ═════
    fun isTorchOn(context: Context): Boolean = TorchState.isOn

    fun toggleTorch(context: Context) {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cm.cameraIdList.firstOrNull {
                cm.getCameraCharacteristics(it)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return
            val newState = !TorchState.isOn
            cm.setTorchMode(cameraId, newState)
            TorchState.isOn = newState
        } catch (e: Exception) {
            Log.w("QuickToggles", "torch failed", e)
        }
    }

    // ═════ Silent ═════
    fun isSilentOn(context: Context): Boolean = try {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.ringerMode == AudioManager.RINGER_MODE_SILENT ||
                am.ringerMode == AudioManager.RINGER_MODE_VIBRATE
    } catch (_: Exception) { false }

    fun toggleSilent(context: Context) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            when (am.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL ->
                    am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE ->
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                else ->
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        } catch (e: Exception) {
            Log.w("QuickToggles", "silent failed", e)
        }
    }
}