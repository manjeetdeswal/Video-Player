package com.manjeet_deswal.video_player_lite.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.media.AudioManager
import android.os.Build
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.lang.reflect.Method


fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}
const val CONTROLLER_TIMEOUT = 3500
fun pxToDp(px: Float): Float {
    return px / Resources.getSystem().displayMetrics.density
}
fun hideSystemBars(activity: Activity) {
    val windowInsetsController =
        ViewCompat.getWindowInsetsController(activity.window.decorView) ?: return
    // Configure the behavior of the hidden system bars
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    // Hide both the status bar and the navigation bar
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}



fun formatMilis(time: Long): String {
    val totalSeconds = Math.abs(time.toInt() / 1000)
    val seconds = totalSeconds % 60
    val minutes = totalSeconds % 3600 / 60
    val hours = totalSeconds / 3600
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds) else String.format(
        "%02d:%02d",
        minutes,
        seconds
    )
}

fun formatMillsSign(time: Long): String {
    return if (time > -1000 && time < 1000) formatMilis(time) else (if (time < 0) "−" else "+") + formatMilis(
        time
    )
}
private fun getVolume(context: Context, max: Boolean, audioManager: AudioManager): Int {
    if (Build.VERSION.SDK_INT >= 30 && Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
        try {
            val method: Method
            var result: Any?
            val clazz = Class.forName("com.samsung.android.media.SemSoundAssistantManager")
            val constructor = clazz.getConstructor(Context::class.java)
            val getMediaVolumeInterval = clazz.getDeclaredMethod("getMediaVolumeInterval")
            result = getMediaVolumeInterval.invoke(constructor.newInstance(context))
            if (result is Int) {
                val mediaVolumeInterval = result
                if (mediaVolumeInterval < 10) {
                    method = AudioManager::class.java.getDeclaredMethod(
                        "semGetFineVolume",
                        Int::class.javaPrimitiveType
                    )
                    result = method.invoke(audioManager, AudioManager.STREAM_MUSIC)
                    if (result is Int) {
                        return if (max) {
                            150 / mediaVolumeInterval
                        } else {
                            result / mediaVolumeInterval
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
    }
    return if (max) {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    } else {
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }
}


fun isVolumeMax(audioManager: AudioManager): Boolean {
    return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == audioManager.getStreamMaxVolume(
        AudioManager.STREAM_MUSIC
    )
}

fun isVolumeMin(audioManager: AudioManager): Boolean {
    val min =
        if (Build.VERSION.SDK_INT >= 28) audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC) else 0
    return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == min
}

