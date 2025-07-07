package com.manjeet_deswal.video_player_lite.ui

import android.annotation.SuppressLint
import android.content.ContentUris
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.data.Folder
import com.manjeet_deswal.video_player_lite.databinding.ActivityVideoListBinding
import com.manjeet_deswal.video_player_lite.rec.VideoListAdapter

import java.util.*
import kotlin.collections.ArrayList

class VideoList : AppCompatActivity(), androidx.appcompat.widget.SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityVideoListBinding
    private var videoList = ArrayList<Folder>()
    private lateinit var adapterm: VideoListAdapter
    private lateinit var folder: String
  private  lateinit var sortOrder:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoListBinding.inflate(layoutInflater)
        folder = intent.getStringExtra("folName").toString()
        setContentView(binding.root)
        val perf=getSharedPreferences("folder", MODE_PRIVATE)
            .edit()

        perf.putString("fold",folder)
        perf.apply()


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = folder
        binding.swipe.setOnRefreshListener {
            showVideo()
            binding.swipe.isRefreshing = false
        }

        showVideo()
        val adView = AdView(this)

        adView.setAdSize(AdSize.BANNER)

        adView.adUnitId = getString(R.string.bannerFiles)

        MobileAds.initialize(
            this
        ) { }


        val mAdView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showVideo() {


        videoList = fetchMedia(folder)
        adapterm = VideoListAdapter(videoList, this,0)
        binding.apply {
            recycleView.adapter = adapterm
            recycleView.layoutManager = LinearLayoutManager(this@VideoList)
            adapterm.notifyDataSetChanged()
        }
    }

    private fun fetchMedia(folderName: String): ArrayList<Folder> {

        val perf = getSharedPreferences("menu_pref", MODE_PRIVATE)

        val sort=perf.getString("sort","size")
        sortOrder = if (sort.equals("sortName")){
            MediaStore.MediaColumns.DISPLAY_NAME+" ASC"

        }else if(sort.equals("sortSize")){
            MediaStore.MediaColumns.SIZE+" DESC"
        } else if(sort.equals("sortDate")){
            MediaStore.MediaColumns.DATE_ADDED+" DESC"
        }else{
            MediaStore.MediaColumns.DURATION+" DESC"
        }

        val list = ArrayList<Folder>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Video.Media.DATA + " like?"
        val selectionArgue = arrayOf("%$folderName%")

        val cursor = contentResolver.query(
            uri, null, selection, selectionArgue, sortOrder
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
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val dateAdded =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))

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


                val folder = Folder(thumb.toString(),id, title, displayName, size, duration_formatted, path, dateAdded)

             val test=   path.substring(path.substring(0, path.lastIndexOf("/")).lastIndexOf("/")+1);

               val index = test.lastIndexOf("/")
                val name = test.substring(0,index)

                if (name == folderName)
                   list.add(folder)


            } while (cursor.moveToNext())
            cursor.close()
        }
        return list
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.app_bar_search)
        val searchView: androidx.appcompat.widget.SearchView =
            item?.actionView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val perf = getSharedPreferences("menu_pref", MODE_PRIVATE)
        val edit = perf.edit()

        when (item.itemId) {

            R.id.refresh -> {
                finish()
                startActivity(intent)
            }
            R.id.home->{
                finish()
            }
            R.id.sort_by -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Sort By")
                dialog.setPositiveButton("ok"
                ) { _, _ ->
                    edit.apply()
                    finish()
                    startActivity(intent)
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

    override fun onQueryTextSubmit(p0: String?): Boolean {

        return false

    }

    override fun onQueryTextChange(p0: String?): Boolean {
        val input = p0?.lowercase()
        val files = ArrayList<Folder>()
        for (file in videoList) {
            if (file.name.lowercase().contains(input.toString())) {
                files.add(file)
            }
            adapterm.updateList(files)
        }
        return true
    }
}