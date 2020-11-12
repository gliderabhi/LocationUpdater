package com.example.locationupdater

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng


@SuppressLint("MissingPermission")
fun addGeoFence(latLng: LatLng, radius: Float, context: Context) {

    val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    val geofenceHelperClass: GeofenceHelperClass = GeofenceHelperClass(context)

    val geoFence = geofenceHelperClass.getGeoFence(
        "First geoFence", latLng, radius, Geofence.GEOFENCE_TRANSITION_DWELL or
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
    )
    val geoFencingRequest = geoFence?.let { geofenceHelperClass.getGeoFencingRequest(it) }
    geofencingClient.addGeofences(geoFencingRequest, geofenceHelperClass.getPendingIntents())?.run {
        addOnSuccessListener {
            Log.e("Add Geofence", geoFence?.requestId.toString())
            Toast.makeText(context, "Added geofence at location ${latLng.latitude} and ${latLng.longitude}", Toast.LENGTH_SHORT).show()
        }
        addOnFailureListener{
            val errorMessage = geofenceHelperClass.getErrorCodes(it)
            Log.e(MapsActivity.TAG, errorMessage)

        }
    }
}
