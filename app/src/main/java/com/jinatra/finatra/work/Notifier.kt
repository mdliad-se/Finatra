package com.jinatra.finatra.work

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jinatra.finatra.MainActivity
import com.jinatra.finatra.R

/** Thin wrapper that posts brand notifications, respecting runtime permission. */
object Notifier {

    /**
     * Posts a notification that opens [MainActivity] when tapped. No-ops when the
     * POST_NOTIFICATIONS permission is missing.
     *
     * @param channel target notification-channel id (see [com.jinatra.finatra.FinatraApp]).
     * @param id notification id; reusing the same id replaces the existing notification and PendingIntent.
     * @param title notification title.
     * @param body notification body, also shown expanded via BigTextStyle.
     */
    fun post(context: Context, channel: String, id: Int, title: String, body: String) {
        if (!canPost(context)) return
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pi = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notif = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notif)
    }

    /** True if notifications may be posted: always pre-Android 13, otherwise only with POST_NOTIFICATIONS granted. */
    private fun canPost(context: Context): Boolean =
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
