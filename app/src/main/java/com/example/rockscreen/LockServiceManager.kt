package com.example.rockscreen

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import com.example.rockscreen.service.LockService

abstract class BaseForegroundServiceManager<T : Service>(
    val context: Context,
    val targetClass: Class<T>,
) {
    fun start() = synchronized(this) {
        val intent = Intent(context, targetClass)

        if (!isServiceRunning(context, targetClass)) {
            context.startForegroundService(intent)
        }
    }

    fun stop() = synchronized(this) {
        val intent = Intent(context, targetClass)

        if (isServiceRunning(context, targetClass)) {
            context.stopService(intent)
        }
    }

    fun isServiceRunning(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val runningServices = activityManager?.getRunningServices(Int.MAX_VALUE)

        if (runningServices != null) {
            for (serviceInfo in runningServices) {
                if (targetClass.name == serviceInfo.service.className) {
                    return true
                }
            }
        }
        return false
    }
}

class LockServiceManager(
    applicationContext: Context
) : BaseForegroundServiceManager<LockService>(
    context = applicationContext,
    targetClass = LockService::class.java,
)