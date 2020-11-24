package com.example.locationupdater.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import com.example.locationupdater.geo.GeofenceHelperClass
import com.example.locationupdater.dao.GeoFence
import com.example.locationupdater.firebase.FirebaseTransactions.insertGeoFenceToDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.time.LocalDate
import java.time.LocalDateTime

object extension {
    lateinit var geoFencingClient: GeofencingClient
    val TAG = "Extensions"


    @SuppressLint("MissingPermission")
    fun addGeoFence(latLng: LatLng, radius: Float, id: String, context: Context) {

        //insert the newly created google geofence to the database
        val geoFence2 = GeoFence(latLng.latitude.toFloat(), latLng.longitude.toFloat(), radius, LocalDateTime.now().toString())
        insertGeoFenceToDatabase(geoFence2)

        geoFencingClient = LocationServices.getGeofencingClient(context)
        val geofenceHelperClass = GeofenceHelperClass(context)

        val geoFence = geofenceHelperClass.getGeoFence(
            id, latLng, radius, Geofence.GEOFENCE_TRANSITION_DWELL or
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geoFencingRequest = geoFence?.let { geofenceHelperClass.getGeoFencingRequest(it) }
        geoFencingClient.addGeofences(geoFencingRequest, geofenceHelperClass.getPendingIntents())
            ?.run {
                addOnSuccessListener {
                    Log.e("Add Geofence", geoFence?.requestId.toString())
                    Toast.makeText(
                        context,
                        "Added geofence at location ${latLng.latitude} and ${latLng.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                addOnFailureListener {
                    val errorMessage = geofenceHelperClass.getErrorCodes(it)
                    //Log.e(TAG, errorMessage)

                }
            }
    }


    fun addCircles(latLng: LatLng, radius: Double, mMap: GoogleMap) {
        val circleOptions = CircleOptions()
            .center(latLng)
            .radius(radius)
            .strokeColor(Color.argb(240, 100, 100, 100))
            .fillColor(Color.argb(50, 100, 0, 100))
            .strokeWidth(1f)
        mMap.addCircle(circleOptions)
    }

    fun removeGeoFences(context: Context){
        geoFencingClient = LocationServices.getGeofencingClient(context)
        geoFencingClient.removeGeofences(GeofenceHelperClass(context).getPendingIntents())?.run {
            addOnSuccessListener {
                Log.e(TAG, "removeGeoFences: removed successfully", )

            }
            addOnFailureListener {
                Log.e(TAG, "removeGeoFences: Failed to remove ", )
            }
        }
    }

    fun addMarkers(latLng: LatLng, mMap: GoogleMap) {
        val markerOptions = MarkerOptions().position(latLng)
        mMap.addMarker(markerOptions)
    }
}
