package com.android.app.shoppy.notification

import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.app.shoppy.R
import com.android.app.shoppy.Utils
import com.android.app.shoppy.models.OrderModel
import com.android.app.shoppy.seller.SellerLoginActivity
import com.android.app.shoppy.seller.SellerMainActivity
import com.google.firebase.auth.FirebaseAuth

class NotificationService : Service() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onStartCommand(intents: Intent?, flags: Int, startId: Int): Int {

        val formattedDate = intents?.getStringExtra("date")
        val orderId = intents?.getStringExtra("orderId")
        if(firebaseAuth.currentUser == null){
            val intent = Intent(this,SellerLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent = PendingIntent.getActivity(this,0,intent,0)
            val notificationCompat = NotificationCompat.Builder(this, Utils.NOTIFICATION_ID)
                .setContentText("Order ID : $orderId")
                .setContentTitle("Order Date : $formattedDate")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                  NotificationManagerCompat.from(this).notify(Utils.NOTIFICATION_NOTIFY_ID,notificationCompat)
              } else {
                  startForeground(Utils.NOTIFICATION_NOTIFY_ID,notificationCompat)
              }


        } else {
            val intent = Intent(this,SellerMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent = PendingIntent.getActivity(this,0,intent,0)
            val notificationCompat = NotificationCompat.Builder(this, Utils.NOTIFICATION_ID)
                .setContentText("Order ID : $orderId")
                .setContentTitle("Order Date : $formattedDate")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationManagerCompat.from(this).notify(Utils.NOTIFICATION_NOTIFY_ID,notificationCompat)
            } else {
                startForeground(Utils.NOTIFICATION_NOTIFY_ID,notificationCompat)
            }
        }




        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}