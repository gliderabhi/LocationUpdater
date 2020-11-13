package com.example.locationupdater.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.locationupdater.activities.MapsActivity
import com.example.locationupdater.utils.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeoFenceBroadCastReceiver : BroadcastReceiver() {

    val TAG = "BroadCastReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Toast.makeText(context, "GeoFence broadcast triggered", Toast.LENGTH_SHORT).show()
        val notificationHelper = NotificationHelper(context)
        val geoFecingEvent = GeofencingEvent.fromIntent(intent)
        if(geoFecingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving intent")
            Toast.makeText(context, "Error in broadcast", Toast.LENGTH_SHORT).show()
            return
        }
        val listFiles = geoFecingEvent.triggeringGeofences as ArrayList<Geofence>

        when(geoFecingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, "Exiting geoFence", Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    "Exit notification",
                    "Please use mask if not already done",
                    MapsActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, "Into the geoFence", Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    "Inside the center notification",
                    "Its ok if you dont have mask",
                    MapsActivity::class.java
                )
            }

            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, "Entered Into the geoFence", Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    "Entry notification",
                    "Safe location",
                    MapsActivity::class.java
                )
            }
        }
    }
}
