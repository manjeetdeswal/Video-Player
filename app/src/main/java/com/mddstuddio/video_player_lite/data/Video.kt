package com.mddstuddio.video_player_lite.data

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable


data class Video(
    val uri: String,
    val name: String,
    val duration: String,
    val id: Long,
    val path: String,
    val size :String,
    val dateAdded :String
)


