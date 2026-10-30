package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.LiveStream

object NotificationHelper {
  private const val CHANNEL_ID = "pptv_live_broadcasts"
  private const val CHANNEL_NAME = "IP TV Live Broadcasts & Alerts"
  private const val CHANNEL_DESC = "Notifications for newly added live streams and breaking broadcasts"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        enableLights(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun sendLiveStreamNotification(context: Context, stream: LiveStream) {
    createNotificationChannel(context)

    // Check permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("EXTRA_STREAM_ID", stream.id)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      stream.id.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("🔴 LIVE NOW on IP TV: ${stream.title}")
      .setContentText("${stream.broadcasterName} is broadcasting live! Tap to watch instantly.")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("${stream.broadcasterName} just published a new live broadcast: \"${stream.title}\". Stream Link is active and broadcasting live now on IP TV!")
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    try {
      NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    } catch (_: Throwable) {
      // Notification failed or permission denied
    }
  }
}
