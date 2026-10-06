package com.example.arklauncher.data

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object LauncherState {

    private var appContext: Context? = null

    private val _status = mutableStateOf(
        LauncherStatus(
            time = "--:--",
            date = "",
            period = "",
            battery = 0,
            dayMode = DayMode.DAY
        )
    )

    val status: State<LauncherStatus> get() = _status

    fun init(context: Context) {
        appContext = context.applicationContext
        update()
    }

    fun update() {
        val ctx = appContext ?: return
        _status.value = LauncherStatusProvider.getStatus(ctx)
    }
}