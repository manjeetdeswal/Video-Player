package com.mddstuddio.video_player_lite.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

class BrightNess_Dialog : AppCompatDialogFragment() {

    private var cross: ImageView? = null
    private var volume_no: TextView? = null
    private var seekBar: SeekBar? = null
    var audioManager: AudioManager? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(com.mddstuddio.video_player_lite.R.layout.brightness_dialog, null)
        builder.setView(view)
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        cross = view.findViewById(com.mddstuddio.video_player_lite.R.id.vol_close)
        volume_no = view.findViewById(com.mddstuddio.video_player_lite.R.id.vol_per)
        seekBar = view.findViewById(com.mddstuddio.video_player_lite.R.id.vol_seek)
       val brightness=Settings.System.getInt(context?.contentResolver,
       Settings.System.SCREEN_BRIGHTNESS,0)

        volume_no!!.text=brightness.toString()
        seekBar!!.progress=brightness
        seekBar!!.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val context=getContext()?.applicationContext
                 val canWrite=Settings.System.canWrite(context)
                if (canWrite){
                    val sbrgiht= p1*255/255
                    volume_no!!.text=sbrgiht.toString()
                    Settings.System.putInt(context?.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                    Settings.System.putInt(context?.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,sbrgiht)


                }else{
                    Toast.makeText(context, "Enable Write Setting for Brightness Control", Toast.LENGTH_SHORT).show()
                    val intent=Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data= Uri.parse("package:"+context?.packageName)
                    startActivityForResult(intent,0)


                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }



        })
        cross!!.setOnClickListener{
            dismiss()
        }



        return builder.create()
    }
}