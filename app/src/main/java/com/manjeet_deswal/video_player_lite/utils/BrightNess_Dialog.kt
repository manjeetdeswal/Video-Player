package com.manjeet_deswal.video_player_lite.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.ui.VideoPlayerActivity

class BrightNessDialog(private val myActivity: Activity) : AppCompatDialogFragment() {

    private var cross: ImageView? = null
    private var volumeNo: TextView? = null
    private var seekBar: SeekBar? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.brightness_dialog, null)
        builder.setView(view)

        cross = view.findViewById(R.id.vol_close)
        volumeNo = view.findViewById(R.id.vol_per)
        seekBar = view.findViewById(R.id.vol_seek)


     seekBar!!.progress = VideoPlayerActivity.brightnessInt

        volumeNo!!.text = VideoPlayerActivity.brightnessInt.toString()
        seekBar!!.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                VideoPlayerActivity.brightnessInt = p1
                VideoPlayerActivity.setScreenBrightness(p1,myActivity)

             volumeNo!!.text = p1.toString()

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