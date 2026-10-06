package com.example.arklauncher.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.ui.geometry.Offset

@Composable
fun rememberTilt(
    xSensitivity: Float = 1.0f,
    ySensitivity: Float = 0.35f,
    smoothing: Float = 0.85f,
    deadZone: Float = 0.03f,
    updateIntervalMs: Long = 16L              // ← 新增：30fps
): State<Offset> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val offsetState = remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensor == null) {
            onDispose { }
        } else {
            var rawX = 0f
            var rawY = 0f

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    rawX = (-event.values[0] / 9.8f).coerceIn(-1f, 1f)
                    rawY = (event.values[1] / 9.8f).coerceIn(-1f, 1f)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

            val job = scope.launch {
                var smoothX = 0f
                var smoothY = 0f

                while (true) {
                    withFrameNanos { }
                    smoothX = smoothX * smoothing + rawX * (1f - smoothing)
                    smoothY = smoothY * smoothing + rawY * (1f - smoothing)

                    val dx = if (abs(smoothX) < deadZone) 0f else smoothX
                    val dy = if (abs(smoothY) < deadZone) 0f else smoothY

                    val newOffset = Offset(dx * xSensitivity, dy * ySensitivity)
                    if (newOffset.x != offsetState.value.x ||
                        newOffset.y != offsetState.value.y) {
                        offsetState.value = newOffset
                    }
                }
            }

            onDispose {
                job.cancel()
                sm.unregisterListener(listener)
            }
        }
    }

    return offsetState
}