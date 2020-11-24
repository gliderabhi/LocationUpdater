package com.example.locationupdater.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.util.Log
import com.example.locationupdater.activities.MapsActivity
import com.example.locationupdater.firebase.FirebaseTransactions
import com.example.locationupdater.utils.NotificationHelper
import com.example.locationupdater.utils.extension.addGeoFence
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

class LocationProviderBroadcastReceiver : BroadcastReceiver() {

    val TAG = "Broadcast receiver"
    val name : String= "SharePrefName"
    val locationOnOff = "isLocationOnOrOff"
    val isNotifiedForGPSOff = "isNotifiedForGPSOff"
    val isNotifiedForGPSOn = "isNotifiedForGPSOn"
    var notifiOff = true
    var notifiOn = true
    lateinit var sharedPreferences : SharedPreferences
    var locationOnOrOff = false


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: entered")

        //get past data from sharedpref
        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        locationOnOrOff = sharedPreferences.getBoolean(locationOnOff, false)

        //is the notification sent for either case gps on or off
        notifiOff = sharedPreferences.getBoolean(isNotifiedForGPSOff, false)
        notifiOn = sharedPreferences.getBoolean(isNotifiedForGPSOn, false)

        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val message = intent.action
        val notificationHelper = NotificationHelper(context)

        //check if intent is of gps turning on or off and do checks
        when(message) {
            "android.location.PROVIDERS_CHANGED" -> {
                val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val perms = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                locationOnOrOff = perms
            }
        }


        //send notification based on condition if notification was shown and if gps is on or not
        if(locationOnOrOff) {
            if(!notifiOn) {
                NotificationHelper(context).sendHighPriorityNotification(
                    "Location helper",
                    "Tracking location please keep gps on at all times ",
                    MapsActivity::class.java
                )

                //re register the geo-fences
                val listFences = FirebaseTransactions.getAllGeoFences()
                for(fences in listFences) {
                    addGeoFence(LatLng(fences.lat!!.toDouble(), fences.long!!.toDouble()), fences.radius!!, LocalDateTime.now().toString(), context)
                }
                notifiOn = true
                notifiOff = false
            }
        }else {
            if (!notifiOff) {
                NotificationHelper(context).sendHighPriorityNotification(
                    "Location Helper",
                    "Please restart gps to let the app work correctly",
                    MapsActivity::class.java
                )
                notifiOff = true
                notifiOn = false
            }
        }

        //save all the data to be used again when broadcast is send again
        with(sharedPreferences.edit()) {
            putBoolean(locationOnOff, locationOnOrOff)
            putBoolean(isNotifiedForGPSOff, notifiOff)
            putBoolean(isNotifiedForGPSOn, notifiOn)
            apply()
        }

    }
}
