package com.example.yandexmap.ui.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

@ExperimentalPermissionsApi
@Composable
fun YandexMap(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationPermission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    val mapView = remember { MapView(context) }
    val mapKit = remember { MapKitFactory.getInstance() }

    val userLocationLayer = remember { mapKit.createUserLocationLayer(mapView.mapWindow) }

    userLocationLayer.isVisible = true
    userLocationLayer.isHeadingEnabled = true

    LaunchedEffect(key1 = Unit, block = {
        locationPermission.launchPermissionRequest()
    })

    if (locationPermission.hasPermission){
        getUserLocation(context)?.let { point ->
            mapView.map.move(
                CameraPosition(point, 16f,0f,0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }
    }

    DisposableEffect(key1 = lifecycleOwner, effect = {

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START){
                mapKit.onStart()
                mapView.onStart()
            }
            if (event == Lifecycle.Event.ON_STOP){
                mapKit.onStop()
                mapView.onStop()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    AndroidView(modifier = modifier, factory = {
        mapView.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    })
}


@SuppressLint("MissingPermission")
private fun getUserLocation(
    context: Context
): Point? {
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