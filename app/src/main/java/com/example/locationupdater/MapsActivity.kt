package com.example.locationupdater

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelperClass: GeofenceHelperClass
    val FINE_LOCATION_ACCESS_CODE= 10
    val BACKGROUND_LOCATION_ACCESS_CODE= 12
    val locationProviderBroadcastReceiver : LocationProviderBroadcastReceiver = LocationProviderBroadcastReceiver()
    val name : String= "SharePrefName"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceHelperClass = GeofenceHelperClass(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(23.0, 87.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in India"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f))
        enableUserLocation()

        mMap.setOnMapLongClickListener(this)
    }

    private fun enableUserLocation () {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            val manager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val perms = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if(perms) {
                mMap.isMyLocationEnabled = true
            }else{
               /* val alertDouble = AlertDialog.Builder(this)
                    .setTitle("Permission for gps")
                    .setMessage("Please start gps for app to function correctly")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        enablegps()
                        enableUserLocation()
                    }
                    .setNegativeButton("Ignore") { _, _ ->
                        Toast.makeText(
                            applicationContext,
                            "App wont work please allow gps ",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                alertDouble.show()*/
                enablegps()
                //enableUserLocation()
            }
        }else {
            //ask for permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )){
                //show user dialog why permission is needed
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_CODE
                )
            }
        }

    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == FINE_LOCATION_ACCESS_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //we have permission
                mMap.isMyLocationEnabled = true
            } else {
                //we dont have permission
                Toast.makeText(
                    applicationContext,
                    "Permission not given requesting again ",
                    Toast.LENGTH_SHORT
                ).show()
                enableUserLocation()
            }
            if (requestCode == BACKGROUND_LOCATION_ACCESS_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //we have permission

                } else {
                    //we dont have permission
                    Toast.makeText(
                        applicationContext,
                        "Permission not given requesting again ",
                        Toast.LENGTH_SHORT
                    ).show()
                    enableUserLocation()
                }
            }
        }
    }

    override fun onMapLongClick(p0: LatLng?) {

            if (Build.VERSION.SDK_INT >= 29) {
                //we need background permission
                if(ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) ==
                        PackageManager.PERMISSION_GRANTED) {
                    //radius = showAlertDialogForRadius(p0)
                    handleLongClick(p0)
                }else {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            BACKGROUND_LOCATION_ACCESS_CODE
                        )
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            BACKGROUND_LOCATION_ACCESS_CODE
                        )
                    }
                }
            } else {
                //radius = showAlertDialogForRadius(p0)
                handleLongClick(p0)
        }

    }

    var radius : Double = 10.0
    private fun handleLongClick(p0: LatLng?) {
        if (p0 != null) {
            mMap.clear()
            addMarkers(p0)
            addCircles(p0, 100.0)
            addGeoFence(p0, 100f)
            val sharedPreferences = getSharedPreferences(name, Context.MODE_PRIVATE)

            with(sharedPreferences.edit()) {
                putFloat("latitude", p0.latitude.toFloat())
                putFloat("longitude", p0.longitude.toFloat())
                apply()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun addGeoFence(latLng: LatLng, radius: Float) {
        val geoFence = geofenceHelperClass.getGeoFence(
            "First geoFence", latLng, radius, Geofence.GEOFENCE_TRANSITION_DWELL or
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geoFencingRequest = geoFence?.let { geofenceHelperClass.getGeoFencingRequest(it) }
        geofencingClient.addGeofences(geoFencingRequest, geofenceHelperClass.getPendingIntents())?.run {
            addOnSuccessListener {
                Toast.makeText(
                    this@MapsActivity, "GeoFence added",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("Add Geofence", geoFence?.requestId.toString())

            }
            addOnFailureListener{
                val errorMessage = geofenceHelperClass.getErrorCodes(it)
                Toast.makeText(
                    this@MapsActivity, errorMessage,
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e(Companion.TAG, errorMessage)

            }
        }
    }

    private fun showAlertDialogForRadius(p0: LatLng?): Double {
        var radius1  = 10.0
        val input = EditText(this@MapsActivity)
        val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        val alertDouble = AlertDialog.Builder(this)
            .setTitle("Radius of geoFence")
            .setView(input)
            .setPositiveButton(
                "OK"
            ) { _, _ ->
                radius1 = input.text.toString().toDouble()
                handleLongClick(p0)
            }
            .setNegativeButton("Ignore") { _, _ ->
                Toast.makeText(applicationContext, "Taking default radius ", Toast.LENGTH_SHORT)
                    .show()
            }
        alertDouble.show()
        return radius1
    }

    fun addMarkers(latLng: LatLng) {
        val markerOptions = MarkerOptions().position(latLng)
        mMap.addMarker(markerOptions)
    }

    fun addCircles(latLng: LatLng, radius: Double) {
        val circleOptions = CircleOptions()
            .center(latLng)
            .radius(radius)
            .strokeColor(Color.argb(240, 100, 100, 100))
            .fillColor(Color.argb(50, 100, 0, 100))
            .strokeWidth(1f)
        mMap.addCircle(circleOptions)
    }

    companion object {
        const val TAG  = "MapsActivity"
    }


    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED)
        intentFilter.addAction(Intent.ACTION_REBOOT)
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationProviderBroadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationProviderBroadcastReceiver)
    }

    fun enablegps() {

        val mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000)
            .setFastestInterval(1000)

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)
        settingsBuilder.setAlwaysShow(true)

        val result =
            LocationServices.getSettingsClient(this).checkLocationSettings(settingsBuilder.build())
        result.addOnCompleteListener { task ->

            //getting the status code from exception
            try {
                task.getResult(ApiException::class.java)
            } catch (ex: ApiException) {

                when (ex.statusCode) {

                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {

                        Toast.makeText(this, "GPS IS OFF", Toast.LENGTH_SHORT).show()

                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        val resolvableApiException = ex as ResolvableApiException
                        val REQUEST_CHECK_SETTINGS = 10001
                        resolvableApiException.startResolutionForResult(
                            this, REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Toast.makeText(
                            this,
                            "PendingIntent unable to execute request.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                        Toast.makeText(
                            this,
                            "Something is wrong in your GPS",
                            Toast.LENGTH_SHORT
                        ).show()

                    }


                }
            }


        }


    }
}