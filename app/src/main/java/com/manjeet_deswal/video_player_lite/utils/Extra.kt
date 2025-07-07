package com.manjeet_deswal.video_player_lite.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

open class BrightnessControl(private val activity: Activity) {

        var currentBrightnessLevel = -1



    private var screenBrightness: Float
        get() = activity.window.attributes.screenBrightness
        set(brightness) {
            val lp = activity.window.attributes
            lp.screenBrightness = brightness
            activity.window.attributes = lp
        }


    private fun levelToBrightness(level: Int): Float {
        val d = 0.064 + 0.936 / 30.0 * level.toDouble()
        return (d * d).toFloat()
    }

}

fun normalizeScaleFactor(scaleFactor: Float, min: Float): Float {
    return Math.max(min, Math.min(scaleFactor, 2.0f))
}

fun checkForInternet(context: Context): Boolean {

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}