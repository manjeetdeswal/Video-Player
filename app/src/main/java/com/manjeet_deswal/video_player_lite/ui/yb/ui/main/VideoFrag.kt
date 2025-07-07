package com.manjeet_deswal.video_player_lite.ui.yb.ui.main

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager

import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.data.Folder
import com.manjeet_deswal.video_player_lite.databinding.FragmentVideoBinding


import com.manjeet_deswal.video_player_lite.rec.VideoAdapter
import com.manjeet_deswal.video_player_lite.ui.UrlActivity
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class VideoFrag : Fragment() , SearchView.OnQueryTextListener {
    var arrayList =ArrayList<Folder>()
    private lateinit var binding: FragmentVideoBinding
    private lateinit var adapter:VideoAdapter
  private  lateinit var sortOrder:String
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




        binding.fab.setOnClickListener {

            val intent = Intent(requireContext(), UrlActivity::class.java)

            startActivity(intent)
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showVideo(){
        arrayList=getVideo()

        binding.apply {
            recyleView.layoutManager = LinearLayoutManager(requireActivity())
            adapter = VideoAdapter(requireContext(), arrayList)
            recyleView.adapter =adapter
            recyleView.setHasFixedSize(true)
            adapter!!.notifyDataSetChanged()

        }



    }


    private fun filter(text: String?) {
        val temp: MutableList<Folder> = ArrayList()
        for (d in arrayList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches

            if (text?.let { d.name.lowercase(Locale.getDefault()).contains(it) }!!) {
                temp.add(d)
            }
        }
        //update recyclerview
        adapter.updateList(temp as ArrayList<Folder>)
    }



    @SuppressLint("Range")
    private fun getVideo():ArrayList<Folder> {

          val   arrayList = ArrayList<Folder>()

        val perf = requireActivity().getSharedPreferences("menu_vid", AppCompatActivity.MODE_PRIVATE)

        val sort=perf.getString("sort","sortName")
        sortOrder = if (sort.equals("sortName")){
            MediaStore.MediaColumns.DISPLAY_NAME+" ASC"

        }else if(sort.equals("sortSize")){
            MediaStore.MediaColumns.SIZE+" DESC"
        } else if(sort.equals("sortDate")){
            MediaStore.MediaColumns.DATE_ADDED+" DESC"
        }else{
            MediaStore.MediaColumns.DURATION+" DESC"
        }

            val contentResolver =context?.contentResolver
            val selection = "${MediaStore.Video.Media.DURATION} >= ?"
            val selectionArgs = arrayOf(
                TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS).toString()
            )

            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

            val cursor = contentResolver?.query(collection, null, selection, selectionArgs, sortOrder)
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {

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
                        "$hrs:" + String.format(
                            Locale.UK,
                            "%02d",
                            min
                        ) + ":" + String.format(
                            Locale.UK, "%02d", sec
                        )
                    }

                    val folder =Folder(thumb.toString(),id,title,name,size, duration_formatted, videoPath, mdate)
                    arrayList.add(folder)

                }
                cursor.close()
            }
            return arrayList


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        activity?.menuInflater?.inflate(R.menu.search_menu,menu)
        val searchItem = menu.findItem(R.id.app_bar_search)

        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(this)

        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val perf = requireActivity().getSharedPreferences("menu_vid", AppCompatActivity.MODE_PRIVATE)
        val edit = perf.edit()
        when (item.itemId) {
            R.id.moreApps -> {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://developer?id=" + "The Notes Giver")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/developer?id=" + "The Notes Giver")
                        )
                    )
                }
            }
            R.id.privacy -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://privacypolicythenotesgiver.blogspot.com/2022/09/video-player-lite.html")
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

            R.id.sort_by -> {
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle("Sort By")
                dialog.setPositiveButton("ok"
                ) { _, _ ->
                    edit.apply()
                    requireActivity().finish()
                    requireActivity().startActivity(requireActivity().intent)
                }
                val items = arrayOf(
                    "Name {A to Z}", "Size {Big to Small}",
                    "Date {New To Old}", "Length {Long to Short}"
                )
                dialog.setSingleChoiceItems(items, -1
                ) { _, p1 ->
                    when (p1) {
                        0 -> {
                            edit.putString("sort", "sortName")
                        }

                        1 -> {
                            edit.putString("sort", "sortSize")
                        }

                        2 -> {
                            edit.putString("sort", "sortDate")
                        }

                        3 -> {
                            edit.putString("sort", "sortLength")
                        }


                    }
                }
                dialog.show()
                dialog.setNegativeButton("cancel"
                ) { _, _ -> }
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