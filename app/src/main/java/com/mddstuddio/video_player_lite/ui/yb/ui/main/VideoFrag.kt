package com.mddstuddio.video_player_lite.ui.yb.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.android.gms.ads.AdView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.mddstuddio.video_player_lite.R
import com.mddstuddio.video_player_lite.data.Folder
import com.mddstuddio.video_player_lite.data.Video
import com.mddstuddio.video_player_lite.databinding.FragmentVideoBinding


import com.mddstuddio.video_player_lite.rec.VideoAdapter
import com.mddstuddio.video_player_lite.ui.VideoPlayerActivity
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class VideoFrag : Fragment() , SearchView.OnQueryTextListener {
    var arrayList =ArrayList<Folder>()
    private lateinit var binding: FragmentVideoBinding
    private lateinit var adapter:VideoAdapter

    private lateinit var mAdView: AdView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_video, container, false)
        binding = FragmentVideoBinding.bind(view)

        adapter = VideoAdapter(requireContext(), arrayList)

        showVideo()
        binding.refresh.setOnRefreshListener {
            showVideo()
            binding.refresh.isRefreshing=false
        }

    /*    val adView = AdView(activity)

        adView.adSize = AdSize.BANNER

        adView.adUnitId = getString(R.string.bannerFiles)

        MobileAds.initialize(
            activity
        ) { }


        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)*/


        binding.fab.visibility=View.GONE

        binding.fab.setOnClickListener { view ->

            val pref =context?.getSharedPreferences("vidPos", Context.MODE_PRIVATE)
            val pos=pref?.getInt("postion",1)
            val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
            intent.putExtra("pos",pos)
            startActivity(intent)
        }

        return view
    }

    fun showVideo(){
        arrayList=getVideo()

        binding.apply {
            recyleView.layoutManager = LinearLayoutManager(requireActivity())
            adapter = VideoAdapter(requireContext(), arrayList!!)
            recyleView.adapter =adapter
            recyleView.setHasFixedSize(true)
            adapter!!.notifyDataSetChanged()

        }



    }


    fun filter(text: String?) {
        val temp: MutableList<Folder> = ArrayList()
        for (d in arrayList!!) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches

            if (text?.let { d.name?.lowercase(Locale.getDefault())?.contains(it) }!!) {
                temp.add(d)
            }
        }
        //update recyclerview
        adapter?.updateList(temp as ArrayList<Folder>)
    }



    @SuppressLint("Range")
    fun getVideo():ArrayList<Folder> {

          val   marrayList = ArrayList<Folder>()
            val contentResolver =context?.contentResolver
            val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val selection = "${MediaStore.Video.Media.DURATION} >= ?"
            val selectionArgs = arrayOf(
                TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS).toString()
            )
            val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

            val cursor = contentResolver?.query(collection, null, selection, selectionArgs, sortOrder)
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleCol =cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val date=cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                while (cursor.moveToNext()) {

                    val videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val id = cursor.getLong(idColumn)
                    val title=cursor.getString(titleCol)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getInt(durationColumn)
                    val mdate=cursor.getString(date)
                    val size = cursor.getString(sizeColumn)
                    val thumb =
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    var duration_formatted: String
                    val sec: Int = duration / 1000 % 60
                    val min: Int = duration / (1000 * 60) % 60
                    val hrs: Int = duration / (1000 * 60 * 60)

                    duration_formatted = if (hrs == 0) {
                        min.toString() + ":" + String.format(Locale.UK, "%02d", sec)
                    } else {
                        hrs.toString() + ":" + String.format(
                            Locale.UK,
                            "%02d",
                            min
                        ) + ":" + String.format(
                            Locale.UK, "%02d", sec
                        )
                    }

                    val folder =Folder(thumb.toString(),id,title,name,size, duration_formatted, videoPath, mdate)
                    marrayList.add(folder)

                }
                cursor.close()
            }
            return marrayList


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        activity?.menuInflater?.inflate(com.mddstuddio.video_player_lite.R.menu.search_menu,menu)
        val searchItem = menu!!.findItem(com.mddstuddio.video_player_lite.R.id.app_bar_search)

        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setQueryHint("Search")
        searchView.setOnQueryTextListener(this)

        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.moreApps -> {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://developer?id=" + "MDD Studio")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/developer?id=" + "MDD Studio")
                        )
                    )
                }
            }
            R.id.privacy -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://mddstudioprivacy.blogspot.com/2021/10/video-player-lite-privcay-policy.html")
                    )
                )


            }

            R.id.btn_share->{
                val appPackageName: String? = context?.packageName
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out the App at: https://play.google.com/store/apps/details?id=$appPackageName"
                )
                sendIntent.type = "text/plain"
                startActivity(sendIntent)

            }



        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        filter(query.toString())
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        filter(newText.toString())
        return true
    }



}