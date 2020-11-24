package com.example.locationupdater.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.locationupdater.R
import com.example.locationupdater.broadcasts.LocationProviderBroadcastReceiver

class KeepAliveBroadcastService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(foregroundNotificationId, foregroundNotification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    companion object {
        @JvmStatic
        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, KeepAliveBroadcastService::class.java))
        }

        @JvmStatic
        fun stop(context: Context) {
            context.stopService(Intent(context, KeepAliveBroadcastService::class.java))
        }
    }

    // Foreground service notification =========

    private val foregroundNotificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    private val foregroundNotification by lazy {
        NotificationCompat.Builder(this, foregroundNotificationChannelId)
            .setSmallIcon(R.drawable.ic_tracker)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentTitle("Wear a mask utility")
            .setContentText("PLease wear a mask before moving out of your house")
            .setSound(null)
            .build()
    }
    private val foregroundNotificationChannelName by lazy {
        getString(R.string.sample_service_name)
    }
    private val foregroundNotificationChannelDescription by lazy {
        getString(R.string.sample_service_description)
    }
    private val foregroundNotificationChannelId by lazy {
        "ForegroundServiceSample.NotificationChannel".also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                    if (getNotificationChannel(it) == null) {
                        createNotificationChannel(
                            NotificationChannel(
                            it,
                            foregroundNotificationChannelName,
                            NotificationManager.IMPORTANCE_HIGH
                        ).also {
                            it.description = foregroundNotificationChannelDescription
                            it.lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                            it.vibrationPattern = null
                            it.setSound(null, null)
                            it.setShowBadge(false)
                        })
                    }
                }
            }
        }
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
