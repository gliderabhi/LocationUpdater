package com.example.locationupdater.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.locationupdater.R
import com.example.locationupdater.broadcasts.LocationProviderBroadcastReceiver
import com.example.locationupdater.dao.GeoFence
import com.example.locationupdater.firebase.FirebaseTransactions.getAllGeoFences
import com.example.locationupdater.fragment.SettingsFragment
import com.example.locationupdater.geo.GeofenceHelperClass
import com.example.locationupdater.service.KeepAliveBroadcastService
import com.example.locationupdater.utils.GeoLocationUtil
import com.example.locationupdater.utils.extension.addCircles
import com.example.locationupdater.utils.extension.addGeoFence
import com.example.locationupdater.utils.extension.addMarkers
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_maps.*
import java.time.LocalDateTime

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener {


    val TAG  = "MapsActivity"
    private lateinit var mMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelperClass: GeofenceHelperClass
    val FINE_LOCATION_ACCESS_CODE= 10
    val BACKGROUND_LOCATION_ACCESS_CODE= 12
    val locationProviderBroadcastReceiver : LocationProviderBroadcastReceiver = LocationProviderBroadcastReceiver()
    private lateinit var fabCurrentLocation : FloatingActionButton
    private lateinit var settings : FloatingActionButton
    var isFragmentVisible = false
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var viewInAnimation :Animation
    private lateinit var viewOutAnimation : Animation
    private var listFences : List<GeoFence> ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        startService(Intent(applicationContext, KeepAliveBroadcastService::class.java))
        intializeVIew()
    }

    private fun intializeVIew() {
        val fragmentHolder = findViewById<FrameLayout>(R.id.fragmentHolder)
        fabCurrentLocation = findViewById(R.id.currentLocationButton)
        settings = findViewById(R.id.settings)

        sharedPreferences = getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE)
        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceHelperClass = GeofenceHelperClass(this)

        //animations for the fragment
        viewInAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        viewInAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                fragmentHolder.visibility = View.VISIBLE
            }
        })
        viewOutAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)
        viewOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onAnimationEnd(animation: Animation?) {
                fragmentHolder.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
    //                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        //click setting to open or close the fragment
        settings.setOnClickListener {
            //open fragment with transition
            if (!isFragmentVisible) {
                supportFragmentManager.beginTransaction().add(
                    R.id.fragmentHolder,
                    SettingsFragment()
                ).commit()

                //animate visibility gone
                fragmentHolder.startAnimation(viewInAnimation)

            } else {
                supportFragmentManager.beginTransaction().remove(SettingsFragment()).commit()
                fragmentHolder.startAnimation(viewOutAnimation)
            }

            isFragmentVisible = !isFragmentVisible
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in my current location from shared pref and move the camera
        val lat = sharedPreferences.getFloat("latitude", 22.793f)
        val long = sharedPreferences.getFloat("longitude", 86.242f)
        var myLocation = LatLng(lat.toDouble(), long.toDouble())
        mMap.addMarker(MarkerOptions().position(myLocation).title("Marker in India"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16f))

        //floating button for current location
       /* fabCurrentLocation.setOnClickListener{
            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val perms = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (perms) {
                val loc = mMap.myLocation
                Toast.makeText(
                    applicationContext,
                    "${loc.latitude} + ${loc.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
                myLocation = LatLng(loc.latitude, loc.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16f))
            } else {
                enablegps()
                //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in India"))
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16f))

            }
        }
       */
        fabCurrentLocation.setOnClickListener{
            checkPermissions()
        }

        getAllGeoFences(mMap, applicationContext)
       // enableUserLocation()
        mMap.uiSettings.isMyLocationButtonEnabled = false
        //insert geofences and some ui changes
        mMap.setOnMapLongClickListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun checkPermissions() {
        //only checking once because after permitting user should click the fab again and this will be called again

        //check if gps is on or off, if off please start it
        if (GeoLocationUtil.isGpsEnabled(applicationContext)) {

            //do i have the permissions if not ask
             if (GeoLocationUtil.hasGpsPermissions(applicationContext)) {
                mMap.isMyLocationEnabled = true
                val myloc = mMap.myLocation
                if (myloc != null) {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                myloc.latitude,
                                myloc.longitude
                            ), 16f
                        )
                    )

                }
            }else {
                GeoLocationUtil.askForLocationPermission(this, FINE_LOCATION_ACCESS_CODE)
            }
        } else {
            enablegps()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == FINE_LOCATION_ACCESS_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //we have permission
                mMap.isMyLocationEnabled = true
            } else {
                //we dont have permission
                Toast.makeText(
                    applicationContext,
                    "Permission not given requesting again ",
                    Toast.LENGTH_SHORT
                ).show()
                //enableUserLocation()
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
                    //enableUserLocation()
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

            //this is in the extensions file
            val radius = sharedPreferences.getFloat(getString(R.string.radius), 10f)
            addMarkers(p0, mMap)
            addCircles(p0, radius.toDouble(), mMap)
            addGeoFence(p0, radius, LocalDateTime.now().toString(),applicationContext)

            //save the geofence in shared preferences
            with(sharedPreferences.edit()) {
                putFloat("latitude", p0.latitude.toFloat())
                putFloat("longitude", p0.longitude.toFloat())
                apply()
            }
        }
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

    override fun onClick(v: View?) {
        Toast.makeText(applicationContext, "Settings clicked ", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        supportFragmentManager.beginTransaction().remove(SettingsFragment()).commitNow()
        if (isFragmentVisible) {
            fragmentHolder.startAnimation(viewOutAnimation)
            isFragmentVisible = !isFragmentVisible
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}