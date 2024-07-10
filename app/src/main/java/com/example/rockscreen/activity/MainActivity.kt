package com.example.rockscreen.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.rockscreen.LockServiceManager

object PermissionUtil {
    fun onObtainingPermissionOverlayWindow(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        launcher.launch(intent)
    }


    fun alertPermissionCheck(context: Context?): Boolean {
        return !Settings.canDrawOverlays(context)
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var lockServiceManager: LockServiceManager
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // 권한 요청 후에 다시 권한 상태를 확인하고 서비스 시작
                if (PermissionUtil.alertPermissionCheck(this)) {
                    PermissionUtil.onObtainingPermissionOverlayWindow(
                        this,
                        requestPermissionLauncher
                    )
                } else {
                    startLockService()
                    finishAfterTransition()
                }

            }

        if (PermissionUtil.alertPermissionCheck(this)) {
            PermissionUtil.onObtainingPermissionOverlayWindow(this, requestPermissionLauncher)
        } else {
            startLockService()
            finishAfterTransition()
        }
    }

    private fun startLockService() {
        if (!this::lockServiceManager.isInitialized) {
            lockServiceManager = LockServiceManager(applicationContext)
        }

        if (!lockServiceManager.isServiceRunning()) {
            lockServiceManager.start()
        }
    }
}