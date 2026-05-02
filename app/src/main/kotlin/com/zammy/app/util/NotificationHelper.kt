package com.zammy.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zammy.app.MainActivity
import com.zammy.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_NEW_TICKETS = "new_tickets"
        const val CHANNEL_TICKET_UPDATES = "ticket_updates"
    }

    fun createNotificationChannels() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val newTicketsChannel = NotificationChannel(
            CHANNEL_NEW_TICKETS,
            context.getString(R.string.notification_channel_new_tickets),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_new_tickets_desc)
        }

        val updatesChannel = NotificationChannel(
            CHANNEL_TICKET_UPDATES,
            context.getString(R.string.notification_channel_updates),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_updates_desc)
        }

        notificationManager.createNotificationChannel(newTicketsChannel)
        notificationManager.createNotificationChannel(updatesChannel)
    }

    fun showNewTicketNotification(ticketId: Int, ticketTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ticket_id", ticketId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ticketId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_TICKETS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_new_ticket_title))
            .setContentText(
                context.getString(R.string.notification_new_ticket_body, ticketId, ticketTitle)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ticketId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun showTicketUpdateNotification(ticketId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ticket_id", ticketId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ticketId + 10000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TICKET_UPDATES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_updated_ticket_title))
            .setContentText(
                context.getString(R.string.notification_updated_ticket_body, ticketId)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ticketId + 10000, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
