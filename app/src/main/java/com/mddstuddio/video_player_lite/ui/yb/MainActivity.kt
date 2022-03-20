package com.mddstuddio.video_player_lite.ui.yb

import android.content.ActivityNotFoundException

import android.content.Intent
import android.net.Uri
import android.os.Bundle

import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import com.mddstuddio.video_player_lite.databinding.ActivityMain2Binding
import com.mddstuddio.video_player_lite.ui.yb.ui.main.SectionsPagerAdapter





class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolabar)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.mddstuddio.video_player_lite.R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {




        val perf = getSharedPreferences("menu_pref", MODE_PRIVATE)
        val edit = perf.edit()

        when (item.itemId) {
            com.mddstuddio.video_player_lite.R.id.moreApps -> {
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
            com.mddstuddio.video_player_lite.R.id.privacy -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://mddstudioprivacy.blogspot.com/2021/10/video-player-lite-privcay-policy.html")
                    )
                )


            }

            com.mddstuddio.video_player_lite.R.id.btn_share->{
                val appPackageName: String = getPackageName()
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
}