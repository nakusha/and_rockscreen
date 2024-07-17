package com.example.rockscreen.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.rockscreen.LockServiceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

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

    fun requestLocationPermission(activity: Activity, launcher: ActivityResultLauncher<String>) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun locationPermissionCheck(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var lockServiceManager: LockServiceManager
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent>

    private lateinit var requestLocationLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (PermissionUtil.alertPermissionCheck(this)) {
                    PermissionUtil.onObtainingPermissionOverlayWindow(
                        this,
                        requestPermissionLauncher
                    )
                } else {
                    checkLocationPermissionAndFetchLocation()
                }
            }

        requestLocationLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    fetchLastLocation()
                } else {
                    Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

        if (PermissionUtil.alertPermissionCheck(this)) {
            PermissionUtil.onObtainingPermissionOverlayWindow(this, requestPermissionLauncher)
        } else {
            checkLocationPermissionAndFetchLocation()
        }
    }

    private fun checkLocationPermissionAndFetchLocation() {
        if (!PermissionUtil.locationPermissionCheck(this)) {
            PermissionUtil.requestLocationPermission(this, requestLocationLauncher)
        } else {
            fetchLastLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    updateLocationUI(it)
                    startLockService()
                    finishAfterTransition()
                } ?: run {
                    Toast.makeText(this, "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "위치를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

    private fun updateLocationUI(location: Location) {
        val latLng = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        Toast.makeText(this, latLng, Toast.LENGTH_SHORT).show()
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