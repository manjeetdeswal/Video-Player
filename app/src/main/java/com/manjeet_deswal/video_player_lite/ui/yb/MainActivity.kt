package com.manjeet_deswal.video_player_lite.ui.yb

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.databinding.ActivityMain2Binding
import com.manjeet_deswal.video_player_lite.ui.yb.ui.main.SectionsPagerAdapter


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

       // requestNotificationPermission()
/*

        val adView = AdView(this)

        adView.setAdSize(AdSize.BANNER)

        adView.adUnitId = getString(R.string.bannerFiles)

        MobileAds.initialize(
            this
        ) { }

// TODO: Add adView to your view hierarchy.

// TODO: Add adView to your view hierarchy.
        val mAdView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

*/


    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {







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
                val appPackageName: String = packageName
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
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}