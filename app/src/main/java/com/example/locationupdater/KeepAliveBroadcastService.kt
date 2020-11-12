package com.example.locationupdater

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.IBinder

class KeepAliveBroadcastService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    val locationProviderBroadcastReceiver : LocationProviderBroadcastReceiver = LocationProviderBroadcastReceiver()

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED)
        intentFilter.addAction(Intent.ACTION_REBOOT)
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationProviderBroadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationProviderBroadcastReceiver)
    }
}
