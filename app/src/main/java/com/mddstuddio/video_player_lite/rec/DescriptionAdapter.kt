package com.mddstuddio.video_player_lite.rec

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import java.io.IOException
import java.net.URL

class DescriptionAdapter(val mediaSessionCompat: MediaSessionCompat) :PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence {

        return mediaSessionCompat.controller.metadata.description.title.toString()
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return null
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return mediaSessionCompat.controller.metadata.description.mediaDescription.toString()
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        return mediaSessionCompat.controller.metadata.description.iconBitmap
    }

    fun playerImage(): Bitmap? {
        var image: Bitmap? = null
        try {
            var url = URL("content image url")
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {

        }
        return image
    }
}