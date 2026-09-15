package com.socialvibe.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.socialvibe.app.MainActivity
import com.socialvibe.app.network.NetworkConfig
import com.socialvibe.app.network.SocketManager
import com.socialvibe.app.network.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Exists only because we deliberately have no FCM/Google Play Services in
// this app (see backend/README.md) — this is the "keep a live connection
// open" approach Signal uses for its no-Google build, traded off against
// battery life. This is the least verifiable file in the whole project:
// Service lifecycle, notification channels and foreground-service-type
// enforcement are 100% Android-framework behavior with no way to compile
// or run outside real Android tooling, unlike the network layer (which was
// actually run against the live backend before being ported in). Expect to
// debug this one for real, especially around Android 14+'s stricter
// foreground service type rules.
class MessagingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob())
    private val socket = SocketManager(NetworkConfig.BASE_URL)
    private var nextNotificationId = MESSAGE_NOTIFICATION_ID_START

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startAsForeground()
        connectAndListen()
    }

    private fun startAsForeground() {
        val notification = buildServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(SERVICE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(SERVICE_NOTIFICATION_ID, notification)
        }
    }

    private fun connectAndListen() {
        serviceScope.launch {
            val tokenStore = TokenStore(applicationContext)
            val session = tokenStore.session.first()
            if (session == null) {
                stopSelf()
                return@launch
            }

            socket.connect(session.accessToken)
            socket.on("message:new").collect { json ->
                val receiverId = json.optString("receiverId")
                val senderId = json.optString("senderId")
                val text = json.optString("text")
                // Only notify for messages addressed to us — this event
                // also fires for messages we sent ourselves (server echoes
                // to both sides so multi-device sessions stay in sync).
                if (receiverId == session.userId && senderId.isNotEmpty()) {
                    showMessageNotification(text)
                }
            }
        }
    }

    private fun showMessageNotification(text: String) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, MESSAGES_CHANNEL_ID)
            .setContentTitle("New message")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        getSystemService(NotificationManager::class.java)?.notify(nextNotificationId++, notification)
    }

    private fun buildServiceNotification(): Notification =
        NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setContentTitle("SocialVibe")
            .setContentText("Staying connected so messages arrive instantly")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(SERVICE_CHANNEL_ID, "Background connection", NotificationManager.IMPORTANCE_MIN)
        )
        manager.createNotificationChannel(
            NotificationChannel(MESSAGES_CHANNEL_ID, "New messages", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        socket.disconnect()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val SERVICE_CHANNEL_ID = "socialvibe_connection"
        private const val MESSAGES_CHANNEL_ID = "socialvibe_messages"
        private const val SERVICE_NOTIFICATION_ID = 1
        private const val MESSAGE_NOTIFICATION_ID_START = 1000

        fun start(context: android.content.Context) {
            val intent = Intent(context, MessagingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: android.content.Context) {
            context.stopService(Intent(context, MessagingService::class.java))
        }
    }
}
