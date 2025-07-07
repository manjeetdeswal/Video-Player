package com.manjeet_deswal.video_player_lite.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar

import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.databinding.ActivityUrlBinding
import com.manjeet_deswal.video_player_lite.utils.checkForInternet


class UrlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUrlBinding

    companion object{
//        var linkToPlay: String = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        var linkToPlay: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Play Url"

        binding.playBtn.setOnClickListener {
            if(checkForInternet(this)){
                val url = binding.addedLink.text.toString()
                val isValid = URLUtil.isValidUrl(url)
                if(url.isNotEmpty() && isValid){
                    if(url.contains("youtu",true)){
                        Snackbar.make(binding.root, "Use Youtube Feature To Play It. \uD83D\uDC49", 5000)
                            .setAction("Play"){
                                val builder = CustomTabsIntent.Builder()
                                @Suppress("DEPRECATION")
                                builder.setToolbarColor(MaterialColors.getColor(this, R.attr.themeColor, Color.RED))
                                val customTabsIntent = builder.build()
                                customTabsIntent.intent.`package` = "com.android.chrome"
                                customTabsIntent.launchUrl(this, Uri.parse(url))
                            }
                            .show()
                    }
                    else {

                        linkToPlay = url
                        startActivity(Intent(this, UrlPlayerActivity::class.java))

                    }
                }
                else Snackbar.make(binding.root, "Enter A Proper Link First!! \uD83D\uDE03", 3000).show()
            }else Snackbar.make(binding.root, "\uD83C\uDF10 Internet Not Connected.", 3000).show()
        }
    }
}