package com.example.locationupdater.firebase

import android.content.Context
import android.util.Log
import com.example.locationupdater.dao.GeoFence
import com.example.locationupdater.utils.extension.addCircles
import com.example.locationupdater.utils.extension.addGeoFence
import com.example.locationupdater.utils.extension.addMarkers
import com.example.locationupdater.utils.extension.removeGeoFences
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*

object FirebaseTransactions {
    private val ref = FirebaseDatabase.getInstance().reference
    private val TAG = "FirebaseTransactions"

    fun insertGeoFenceToDatabase(geofence: GeoFence) {
       ref.child("geofences").push().setValue(geofence).addOnSuccessListener {
           Log.e(TAG, "insertGeoFenceToDatabase: Inserted the geofence object", )
       }
    }

    fun getAllGeoFences(mMap: GoogleMap, context: Context) : List<GeoFence> {
        val fences = ArrayList<GeoFence>()
        ref.child("geofences").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()
                for (snaps in snapshot.children) {
                    val fence = snaps.getValue(GeoFence::class.java)
                    val latLng = LatLng(fence!!.lat!!.toDouble(), fence.long!!.toDouble())
                    addMarkers(latLng, mMap)
                    addCircles(latLng, fence.radius!!.toDouble(), mMap)
                    Log.e(TAG, "onDataChange: ${fence.long}",)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}", )
            }
        })

        for(fenceItem in fences) {
            addGeoFence(LatLng(fenceItem.lat!!.toDouble(),fenceItem.long!!.toDouble()), fenceItem.radius!!.toFloat(),fenceItem.id!! , context)
            Log.e(TAG, "getAllGeoFences: fence added ? ", )
        }
        return fences
    }

    fun getAllGeoFences() : List<GeoFence> {
        val fences = ArrayList<GeoFence>()
        ref.child("geofences").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snaps in snapshot.children) {
                    val fence = snaps.getValue(GeoFence::class.java)
                    val latLng = LatLng(fence!!.lat!!.toDouble(), fence.long!!.toDouble())
                    fences.add(fence)
                    Log.e(TAG, "onDataChange: ${fence.long}",)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}", )
            }
        })
        return fences
    }

    fun clearAllGeoFences(context: Context){
        ref.child("geofences").removeValue()
        removeGeoFences(context)
    }
}