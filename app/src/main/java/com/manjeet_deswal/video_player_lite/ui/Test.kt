package com.manjeet_deswal.video_player_lite.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.manjeet_deswal.video_player_lite.databinding.ActivityTestBinding

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class Test :AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding

    private lateinit var player: ExoPlayer
    companion object{




        private lateinit var trackSelector: DefaultTrackSelector


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*    playerList = ArrayList()

             playerList= MainActivity.videoList*/


        playVideo()
    }

    private fun  playVideo() {
        try {
            player.release()
        } catch (_: Exception) {
        }


        trackSelector =DefaultTrackSelector(this)




        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector).build()
        binding.playerView.player  = player

        val intetn = intent.getStringExtra("class")
        val mediaItem =MediaItem.fromUri("file:///storage/emulated/0/DCIM/SharedFolder/A.Perfect.Story.S01E01.We.Wont.Look.For.Each.Other.720p.x264.English.Spanish.Esubs.MoviesMod.org.mkv")


        player.setMediaItem(mediaItem)

        player.prepare()



        player.play()






    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}