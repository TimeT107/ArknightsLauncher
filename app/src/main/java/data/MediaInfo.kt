package com.example.arklauncher.data

import android.graphics.Bitmap

data class MediaInfo(
    val title: String,
    val artist: String,
    val albumArt: Bitmap?,
    val isPlaying: Boolean,
    val packageName: String?,
    val duration: Long,
    val position: Long
)