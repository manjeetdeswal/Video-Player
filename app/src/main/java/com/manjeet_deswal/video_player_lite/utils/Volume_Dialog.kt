package com.manjeet_deswal.video_player_lite.utils

import androidx.appcompat.app.AppCompatDialogFragment
import android.widget.SeekBar

import android.media.AudioManager



import android.os.Bundle

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar.OnSeekBarChangeListener

import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.manjeet_deswal.video_player_lite.R
import kotlin.math.ceil


class Volume_Dialog:AppCompatDialogFragment() {

    private var cross: ImageView? = null
    private var volume_no: TextView? = null
    private var seekBar: SeekBar? = null
    var audioManager: AudioManager? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_volume_popup, null)
        builder.setView(view)

        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        cross = view.findViewById(R.id.vol_close)
        volume_no = view.findViewById(R.id.vol_per)
        seekBar = view.findViewById(R.id.vol_seek)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        seekBar!!.max = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        seekBar!!.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)

        val mediavolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volPerc = ceil(mediavolume.toDouble() / maxVol.toDouble() * 100.toDouble())

        volume_no!!.text = "$volPerc %"
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                val mediavolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVol = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val volPerc = ceil(mediavolume.toDouble() / maxVol.toDouble() * 100.toDouble())
                volume_no!!.text = "$volPerc %"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        cross!!.setOnClickListener{
            dismiss()
        }
        return builder.create()
    }
}