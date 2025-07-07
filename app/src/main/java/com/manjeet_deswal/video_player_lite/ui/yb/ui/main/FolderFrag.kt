package com.manjeet_deswal.video_player_lite.ui.yb.ui.main

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.data.Folder
import com.manjeet_deswal.video_player_lite.databinding.FragmentFolderBinding
import com.manjeet_deswal.video_player_lite.rec.FolderAdapter


class FolderFrag : Fragment() {

private var mediaList = ArrayList<Folder>()
    private val folderList =ArrayList<String>()
    private lateinit var binding:FragmentFolderBinding
    private lateinit var adapterM: FolderAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_folder, container, false)
        binding= FragmentFolderBinding.bind(view)

        adapterM=FolderAdapter(mediaList,folderList)

        showFolders()
        binding.refresh.setOnRefreshListener {
            showFolders()
            binding.refresh.isRefreshing=false

        }
        return view
    }

    private fun showFolders(){
        mediaList=fetchMedia()
        binding.apply {
            recycleView.adapter =adapterM
            recycleView.layoutManager=LinearLayoutManager(activity)
            adapterM.notifyDataSetChanged()

        }
    }

    private fun fetchMedia():ArrayList<Folder> {





        val list = ArrayList<Folder>()
       val uri =MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor =context?.contentResolver?.query(
            uri,null,null,null,null,null
        )
        if (cursor!= null && cursor.moveToNext()){
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                val size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))

                val thumb = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                val dateAdded = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
                val folder = Folder(
                    thumb.toString(),
                    id,
                    title,
                    displayName,
                    size,
                    (duration ?: "0.0"),
                    path,
                    dateAdded
                )
                val index = path.lastIndexOf("/")
                val exin = path.lastIndexOf(".")
                val str = path.substring(0, index)

                if (!folderList.contains(str)  ) {
                    folderList.add(str)
                }
                list.add(folder)


            }
                while (cursor.moveToNext())
                cursor.close()
        }
        return list
    }


}