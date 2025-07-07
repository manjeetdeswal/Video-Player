package com.manjeet_deswal.video_player_lite.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.data.Folder
import com.manjeet_deswal.video_player_lite.rec.VideoListAdapter


class PlayListDialog(
    private var songList: ArrayList<Folder>,
    var adapter: VideoListAdapter? =null,

    ):BottomSheetDialogFragment() {
    private var bottomSheetDialog:BottomSheetDialog?=null




    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view =LayoutInflater.from(context).inflate(R.layout.playlist,null)
        bottomSheetDialog!!.setContentView(view)


        val pref=context?.getSharedPreferences("folder",Context.MODE_PRIVATE)
      val folder=  pref?.getString("fold","download")
        songList=fetchMedia(folder.toString())


        adapter= VideoListAdapter(songList,requireContext(),1)
        val folderName=view.findViewById<TextView>(R.id.folder_name)
        folderName .text=folder.toString()

        val recyclerView= view.findViewById<RecyclerView>(R.id.playRec)
        recyclerView.layoutManager=LinearLayoutManager(context)
        recyclerView.adapter=adapter


        adapter!!.notifyDataSetChanged()
        return bottomSheetDialog as BottomSheetDialog


    }

    private fun fetchMedia(folderName: String): ArrayList<Folder> {


        val list = ArrayList<Folder>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Video.Media.DATA + " like?"
        val selectionArgu = arrayOf("%$folderName%")
        val cursor = context?.contentResolver?.query(
            uri, null, selection, selectionArgu, null
        )
        if (cursor != null && cursor.moveToNext()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                val displayName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                val size =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                val duration =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val dateAdded =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
                val thumb =
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                val folder = Folder(thumb.toString(),id, title, displayName, size, duration, path, dateAdded)

                list.add(folder)


            } while (cursor.moveToNext())
            cursor.close()
        }
        return list
    }



}