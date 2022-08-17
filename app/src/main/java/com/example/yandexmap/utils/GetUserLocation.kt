package com.example.yandexmap.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import com.yandex.mapkit.geometry.Point

@SuppressLint("MissingPermission")
fun getUserLocation(context: Context):Point? {

    val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    val providers = locationManager.getProviders(true)
    var location: Location? = null

    for (i in providers.indices.reversed()) {
        location = locationManager.getLastKnownLocation(providers[i])
        if (location != null) break
    }

    location?.let {
        return Point(location.latitude, location.longitude)
    }

    return null
}