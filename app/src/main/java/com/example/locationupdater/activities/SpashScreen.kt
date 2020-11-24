package com.example.locationupdater.activities

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.locationupdater.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_spash_screen.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SpashScreen : AppCompatActivity() , Animation.AnimationListener, PermissionListener{

    @SuppressLint("ClickableViewAccessibility", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_spash_screen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        animation.setAnimationListener(this)
        splashImage.startAnimation(animation)
        splashText.startAnimation(animation)
    }

    override fun onAnimationStart(animation: Animation?) {
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onAnimationEnd(animation: Animation?) {
        getPermissions()
        startActivity(Intent(this, HelpActivity::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getPermissions() {
        /*
        val permissions = arraySetOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE,Manifest.permission.INTERNET)*/

        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(this)

/*        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            .withListener(this)

        Dexter.withActivity(this).withPermission(Manifest.permission.FOREGROUND_SERVICE)
            .withListener(this)*/
    }

    override fun onAnimationRepeat(animation: Animation?) {
        Toast.makeText(applicationContext, "Animation should not restart ", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {
        Toast.makeText(applicationContext, "Permission show rationale", Toast.LENGTH_SHORT).show()
    }


}