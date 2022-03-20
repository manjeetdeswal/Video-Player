package com.mddstuddio.video_player_lite.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import com.google.android.exoplayer2.ui.StyledPlayerView

class CustomPlayer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : StyledPlayerView(context, attrs),GestureDetector.OnGestureListener,ScaleGestureDetector.OnScaleGestureListener {

    private val mDetector: GestureDetectorCompat? = null

    private val gestureScrollY = 0f
    private val gestureScrollX = 0f
    private val handleTouch = false
    private val seekStart: Long = 0
    private val seekChange: Long = 0
    private val seekMax: Long = 0
    private val canBoostVolume = false
    private val canSetAutoBrightness = false

    private val IGNORE_BORDER: Int = dpToPx(24)
    private val SCROLL_STEP: Int = dpToPx(16)
    private val SCROLL_STEP_SEEK: Int = dpToPx(8)
    private val SEEK_STEP: Long = 1000
    val MESSAGE_TIMEOUT_TOUCH = 400
    val MESSAGE_TIMEOUT_KEY = 800
    val MESSAGE_TIMEOUT_LONG = 1400

    private val restorePlayState = false
    private val canScale = true
    private val isHandledLongPress = false

    private val mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1f
    private val mScaleFactorFit = 0f


    override fun onScale(p0: ScaleGestureDetector?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        if (canScale) {
            val previousScaleFactor = mScaleFactor
            mScaleFactor *= p0?.getScaleFactor()!!
            mScaleFactor = Math.max(0.25f, Math.min(mScaleFactor, 2.0f))
            if (isCrossingThreshold(previousScaleFactor, mScaleFactor, 1.0f) ||
                isCrossingThreshold(previousScaleFactor, mScaleFactor, mScaleFactorFit)
            ) performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            setScale(mScaleFactor)
            restoreSurfaceView()
            clearIcon()
            setCustomErrorMessage((mScaleFactor * 100) as String + "%")
            return true
        }
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {
        TODO("Not yet implemented")
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onShowPress(p0: MotionEvent?) {
        TODO("Not yet implemented")
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun onLongPress(p0: MotionEvent?) {
        TODO("Not yet implemented")
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        TODO("Not yet implemented")
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }


    private fun isCrossingThreshold(val1: Float, val2: Float, threshold: Float): Boolean {
        return val1 < threshold && val2 >= threshold || val1 > threshold && val2 <= threshold
    }


    fun setScale(scale: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val videoSurfaceView = videoSurfaceView
            videoSurfaceView!!.scaleX = scale
            videoSurfaceView.scaleY = scale
            //videoSurfaceView.animate().setStartDelay(0).setDuration(0).scaleX(scale).scaleY(scale).start();
        }
    }
    private fun restoreSurfaceView() {
        if (videoSurfaceView!!.alpha != 1f) {
            videoSurfaceView!!.alpha = 1f
        }
    }
    fun clearIcon() {
        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
       /* exoErrorMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        setHighlight(false)*/
    }


}