package com.mddstuddio.video_player_lite.utils

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus

import com.google.android.gms.ads.initialization.OnInitializationCompleteListener




class MyApp:Application() {
    private var appOpenManager: AppOpenManager? = null
    override fun onCreate() {
        super.onCreate()
      /*  MobileAds.initialize(
            this
        ) { }*/
       // appOpenManager = AppOpenManager(this)
    }

}