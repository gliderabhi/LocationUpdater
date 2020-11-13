package com.example.locationupdater.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object GeoLocationUtil  {

    fun isGpsEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun hasGpsPermissions(context: Context?): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun askForLocationPermission(thisActivity: Activity?, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                thisActivity!!,
                Manifest.permission.READ_CONTACTS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    thisActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                // we force to allow this permission
                ActivityCompat.requestPermissions(
                    thisActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode
                )
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    thisActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

}