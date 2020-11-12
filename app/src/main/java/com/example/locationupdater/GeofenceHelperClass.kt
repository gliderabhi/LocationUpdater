package com.example.locationupdater

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import java.lang.Exception

class GeofenceHelperClass(base: Context?) : ContextWrapper(base){

    public fun getGeoFencingRequest(geofence: Geofence) : GeofencingRequest? {
        return GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()
    }

    public fun getGeoFence(id : String, latLng: LatLng, radius: Float, transitonType : Int) : Geofence?{
        return Geofence.Builder()
            .setCircularRegion(latLng.latitude,latLng.longitude,radius)
            .setRequestId(id)
            .setTransitionTypes(transitonType)
            .setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    companion object {
        const val TAG = "GeoFenceHelper"
    }

    public fun getPendingIntents() : PendingIntent {
        val intent = Intent(this,GeoFenceBroadCastReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 2607,intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }

    lateinit var pendingIntent : PendingIntent

    public fun getErrorCodes(e: Exception) : String {
        val error = e as ApiException
        when(e.statusCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return "GeoFence not available"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return "GEOFENCE_TOO_MANY_GEOFENCES"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return "GEOFENCE_TOO_MANY_PENDING_INTENTS"
        }

        return e.localizedMessage
    }
}