package com.manjeet_deswal.video_player_lite.utils

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.ListenableFuture

class VideoPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    // The MediaSessionService callback
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onCreate() {
        super.onCreate()

        // 1. Create the ExoPlayer instance
        val player = ExoPlayer.Builder(this).build()

        // 2. Create a MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(CustomMediaSessionCallback()) // Important for custom logic
            .build()
    }

    // Custom callback to handle actions like adding media items
    private class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map {
                // Here you could potentially update media items with more metadata
                // if they are being added from a simple URI
                it
            }.toMutableList()
            return super.onAddMediaItems(mediaSession, controller, updatedMediaItems)
        }
    }


    override fun onDestroy() {
        mediaSession?.player?.release()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}