package com.mddstuddio.video_player_lite.utils

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.*

class AppOpenManager(myApplication: MyApp) : ActivityLifecycleCallbacks,
    LifecycleObserver {
    private var appOpenAd: AppOpenAd? = null
    private val currentActivity: Activity? = null
    private var loadCallback: AppOpenAdLoadCallback? = null
    private val myApplication: MyApp
    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        fetchAd()
    }

    /** Request an ad  */
    fun fetchAd() {
        // We will implement this below.
        // Have unused ad, no need to fetch another.
        if (isAdAvailable) {
            return
        }
        loadCallback = object : AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
            }
        }
        val request = adRequest
        AppOpenAd.load(
            myApplication, AD_UNIT_ID, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
        )
    }

    fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable) {
            Log.d(LOG_TAG, "Will show ad.")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        fetchAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd!!.setFullScreenContentCallback(fullScreenContentCallback)
            appOpenAd!!.show(currentActivity)
        } else {
            Log.d(LOG_TAG, "Can not show ad.")
            fetchAd()
        }
    }

    /** Creates and returns ad request.  */
    private val adRequest: AdRequest
        private get() = AdRequest.Builder().build()

    /** Utility method that checks if ad exists and can be shown.  */
    val isAdAvailable: Boolean
        get() = appOpenAd != null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        showAdIfAvailable()
        Log.d(LOG_TAG, "onStart")
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        private const val LOG_TAG = "AppOpenManager"
        private const val AD_UNIT_ID = "ca-app-pub-6283712090627179/3673302572"
        private var isShowingAd = false
    }

    /** Constructor  */
    init {
        this.myApplication = myApplication
        this.myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
}

