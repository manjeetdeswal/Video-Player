package com.mddstuddio.video_player_lite.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.mddstuddio.video_player_lite.ui.VideoPlayerActivity

import java.lang.Exception
import kotlin.coroutines.coroutineContext

open class OnSwipeTouchListener(val context: Context?) : View.OnTouchListener {

    var gestureDetector: GestureDetector? = null



    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        gestureDetector = GestureDetector(context, GestureListener(context!!))
        v?.performClick()
        return gestureDetector!!.onTouchEvent(event)
    }


    private class GestureListener(val context:Context) : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
        fun onSwipeRight() {
            Toast.makeText(context, "right", Toast.LENGTH_SHORT).show()
        }

        fun onSwipeLeft() {
            Toast.makeText(context, "left", Toast.LENGTH_SHORT).show()

        }

        fun onSwipeTop() {
            Toast.makeText(context, "top", Toast.LENGTH_SHORT).show()

        }

        fun onSwipeBottom() {
            Toast.makeText(context, "bottom", Toast.LENGTH_SHORT).show()

        }

        companion object {
            private const val SWIPE_THRESHOLD = 100
            private const val SWIPE_VELOCITY_THRESHOLD = 100
        }


    }


}
