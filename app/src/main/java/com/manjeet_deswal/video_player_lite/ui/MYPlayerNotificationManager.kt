package com.manjeet_deswal.video_player_lite.ui

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import com.manjeet_deswal.video_player_lite.ui.VideoPlayerActivity.Companion.player
import com.manjeet_deswal.video_player_lite.ui.yb.MainActivity

@UnstableApi class MYPlayerNotificationManager ( player: ExoPlayer): Service() {
    private lateinit var playerNotificationManager: PlayerNotificationManager

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        initNotification(this, player)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initNotification(context: Context, player: ExoPlayer) {
        val mediaDescriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return "Playing: ${player.currentMediaItem?.mediaMetadata?.title ?: "Video"}"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(context, MainActivity::class.java)
                return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return player.currentMediaItem?.mediaMetadata?.description
            }

            override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                return null // Add thumbnail bitmap if needed
            }
        }

        playerNotificationManager = PlayerNotificationManager.Builder(
            context,
            1, // Notification ID
            "media_channel_id" // Notification channel ID
        )
            .setChannelImportance(NotificationManager.IMPORTANCE_LOW)
            .setMediaDescriptionAdapter(mediaDescriptionAdapter)
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopForeground(true)
                }

                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                   startForeground(notificationId, notification)


                }
            })
            .build()

        playerNotificationManager.setPlayer(player)
        playerNotificationManager.setUseNextAction(true)
        playerNotificationManager.setUsePreviousAction(true)
        playerNotificationManager.setUseFastForwardAction(true)
        playerNotificationManager.setUseRewindAction(true)
        //playerNotificationManager.setFastForwardIncrementMs(10_000)
       // playerNotificationManager.setRewindIncrementMs(10_000)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}