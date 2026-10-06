package com.example.arklauncher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.example.arklauncher.service.MediaNotificationListener

object MediaSessionHelper {

    private fun componentName(context: Context) =
        ComponentName(context, MediaNotificationListener::class.java)

    fun hasPermission(context: Context): Boolean = try {
        val enabled = NotificationManagerCompat.getEnabledListenerPackages(context)
        enabled.contains(context.packageName)
    } catch (_: Exception) {
        false
    }

    private fun getSessions(context: Context): List<android.media.session.MediaController> {
        if (!hasPermission(context)) return emptyList()
        val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            ?: return emptyList()
        return try {
            msm.getActiveSessions(componentName(context))
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    private fun pickSession(context: Context): android.media.session.MediaController? {
        val sessions = getSessions(context)
        return sessions.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: sessions.firstOrNull()
    }

    fun getCurrentMedia(context: Context): MediaInfo? {
        val session = pickSession(context) ?: return null
        val metadata = session.metadata ?: return null
        val state = session.playbackState

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return null
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: ""
        val albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val isPlaying = state?.state == PlaybackState.STATE_PLAYING

        val position = if (state != null) {
            if (isPlaying) {
                val elapsed = SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
                (state.position + elapsed).coerceIn(0L, duration)
            } else {
                state.position.coerceIn(0L, duration)
            }
        } else 0L

        return MediaInfo(
            title = title,
            artist = artist,
            albumArt = albumArt,
            isPlaying = isPlaying,
            packageName = session.packageName,
            duration = duration,
            position = position
        )
    }

    fun togglePlayPause(context: Context) {
        val session = pickSession(context) ?: return
        if (session.playbackState?.state == PlaybackState.STATE_PLAYING) {
            session.transportControls.pause()
        } else {
            session.transportControls.play()
        }
    }

    fun next(context: Context) {
        pickSession(context)?.transportControls?.skipToNext()
    }

    fun previous(context: Context) {
        pickSession(context)?.transportControls?.skipToPrevious()
    }

    fun openPlayerApp(context: Context) {
        val pkg = pickSession(context)?.packageName ?: return
        AppLauncher.launchPackage(context, pkg)
    }

    fun openNotificationAccessSettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {}
        }
    }
}