package com.manjeet_deswal.video_player_lite.utils.dtpv

interface SeekListener {
    /**
     * Called when video start reached during rewinding
     */
    fun onVideoStartReached() {}

    /**
     * Called when video end reached during forwarding
     */
    fun onVideoEndReached() {}
}