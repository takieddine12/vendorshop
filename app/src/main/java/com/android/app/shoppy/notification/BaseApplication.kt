package com.android.app.shoppy.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.StrictMode
import com.android.app.shoppy.Utils

class BaseApplication : Application() {

    override fun onCreate() {
        catchAccidentalTasksOnUi()
        super.onCreate()
        createNotification()
    }

    private fun catchAccidentalTasksOnUi(){
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectAll()
                .penaltyLog()
                .build())
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build())
    }

    private fun createNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel  = NotificationChannel(
                Utils.NOTIFICATION_ID,Utils.NOTIFICATION_STRING,NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(500,1000,1000,500)

            val notificationManager  = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}