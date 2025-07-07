package com.manjeet_deswal.video_player_lite.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build


class MyApp:Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "media_channel_id",
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


}