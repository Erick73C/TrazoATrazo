package com.example.trazoatrazo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.trazoatrazo.R

/**
 * Utilidad para gestionar las notificaciones locales del sistema.
 * Configura canales (Android 8+) y construye las notificaciones de eventos.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "special_events_channel"
    private const val CHANNEL_NAME = "Eventos Especiales"
    private const val CHANNEL_DESC = "Notificaciones sobre eventos de temporada y dibujos nuevos"

    /** Crea el canal de notificación si es necesario (Android Oreo o superior). */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                // VISIBILITY_PRIVATE: Solo título e icono en la pantalla de bloqueo
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Muestra una notificación sobre un evento especial activo. */
    fun showEventNotification(context: Context, eventName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_recuerdos_logo) // Icono de la app solicitado
            .setContentTitle(eventName)
            .setContentText("¡El evento ha comenzado! La app se ha personalizado y hay nuevos dibujos temáticos por desbloquear.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            // VISIBILITY_PRIVATE asegura que en pantalla de bloqueo solo se vea lo básico
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        notificationManager.notify(eventName.hashCode(), builder.build())
    }

    /** Muestra una notificación cuando se desbloquea un dibujo nuevo. */
    fun showUnlockNotification(context: Context, drawingTitle: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_recuerdos_logo)
            .setContentTitle("¡Nuevo dibujo desbloqueado!")
            .setContentText("Se ha desbloqueado un dibujo nuevo: $drawingTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        notificationManager.notify(drawingTitle.hashCode(), builder.build())
    }
}
