package com.example.rockscreen.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.IBinder
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.rockscreen.LockServiceManager
import com.example.rockscreen.R
import com.example.rockscreen.receiver.LockReceiver

class LockService : Service() {

    private lateinit var lockServiceManager: LockServiceManager
    private lateinit var lockReceiver: LockReceiver

    override fun onCreate() {
        super.onCreate()
        lockServiceManager = LockServiceManager(applicationContext) // 직접 객체 생성
        lockReceiver = LockReceiver() // LockReceiver 인스턴스 생성
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(SERVICE_ID, createNotificationBuilder())
        startLockReceiver()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopLockReceiver()
        lockServiceManager.stop()
        super.onDestroy()
    }

    private fun startLockReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(lockReceiver, intentFilter)
    }

    private fun stopLockReceiver() {
        unregisterReceiver(lockReceiver)
    }

    private fun createNotificationChannel() {
        val notificationChannel = SimpleNotificationBuilder.createChannel(
            LOCK_CHANNEL,
            getStringWithContext(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH,
            getStringWithContext(R.string.lock_screen_description)
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun getStringWithContext(
        @StringRes stringRes: Int
    ): String {
        return applicationContext.getString(stringRes)
    }

    private fun createNotificationBuilder(): Notification {
        return SimpleNotificationBuilder.createBuilder(
            context = this,
            channelId = LOCK_CHANNEL,
            title = getStringWithContext(R.string.app_name),
            text = getStringWithContext(R.string.lock_screen_description),
            icon = R.drawable.ic_launcher_foreground,
        )
    }

    private companion object {
        const val LOCK_CHANNEL = "LOCK_CHANNEL"
        const val SERVICE_ID: Int = 1
    }

    object SimpleNotificationBuilder {
        fun createChannel(
            channelId: String,
            name: String,
            importance: Int = NotificationManager.IMPORTANCE_HIGH,
            description: String
        ) =
            NotificationChannel(channelId, name, importance).apply {
                setShowBadge(false)
                enableLights(true)
                this.description = description
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                lightColor = Color.BLACK
            }

        fun createBuilder(
            context: Context,
            channelId: String,
            title: String,
            text: String,
            @DrawableRes icon: Int,
        ) = Notification.Builder(context, channelId).apply {
            setOngoing(true)
            setShowWhen(true)
            setSmallIcon(icon)
            setContentTitle(title)
            setContentText(text)
        }.build()
    }
}