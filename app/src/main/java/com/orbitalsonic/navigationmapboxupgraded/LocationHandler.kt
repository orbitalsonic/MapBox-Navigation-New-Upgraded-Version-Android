package com.orbitalsonic.navigationmapboxupgraded

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat

object LocationHandler {
    val LOCATION_PERMISSION = 3

    fun Activity?.isGpsEnabled():Boolean{
        this?.let {
            try {
                val locationManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (e: Exception) {
                return false
            }

        }?: run {
            return false
        }

    }

    fun Activity?.isLocationApproved():Boolean{
        this?.let {
            try {
                return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } catch (e: Exception) {
                return false
            }

        }?: run {
            return false
        }

    }
}