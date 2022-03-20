package com.mddstuddio.video_player_lite.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.mddstuddio.video_player_lite.R
import com.mddstuddio.video_player_lite.ui.yb.MainActivity


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ){
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN ,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        var permission_array=arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if((ContextCompat.checkSelfPermission(this,permission_array[0]))==PackageManager.PERMISSION_DENIED){
            requestPermissions(permission_array,0)
        }else{
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            },500)
        }




    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            },500)
        }
        else{
            recreate()
        }
    }


}