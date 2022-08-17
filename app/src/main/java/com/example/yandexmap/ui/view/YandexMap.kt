package com.example.yandexmap.ui.view

import android.Manifest
import android.content.Context
import android.graphics.PointF
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.example.yandexmap.R
import com.yandex.mapkit.user_location.UserLocationLayer

fun rememberCameraPosition(
    cameraPosition: CameraPosition? = null,
    latitude: Double = cameraPosition?.target?.latitude ?: 0.0,
    longitude: Double = cameraPosition?.target?.longitude ?: 0.0,
    zoom: Float = cameraPosition?.zoom ?: 0f,
    azimuth: Float = cameraPosition?.azimuth ?: 0f,
    tilt: Float = cameraPosition?.tilt ?: 0f,
):CameraPosition {
    return CameraPosition(
        Point(latitude,longitude),zoom,azimuth,tilt
    )
}

@ExperimentalPermissionsApi
@Composable
fun YandexMap(
    modifier: Modifier = Modifier,
    cameraPosition: CameraPosition = rememberCameraPosition(),
    userLocation: Boolean = false,
    search: String? = null,
    onCameraPosition: (CameraPosition) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationPermission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    val mapView = remember { MapView(context) }
    val mapKit = remember { MapKitFactory.getInstance() }

    val userLocationLayer = remember { mapKit.createUserLocationLayer(mapView.mapWindow) }

    val searchFactory = remember { SearchFactory.getInstance() }
    val searchManager = remember { searchFactory.createSearchManager(SearchManagerType.COMBINED) }

    userLocationLayer.isVisible = locationPermission.hasPermission
    userLocationLayer.isHeadingEnabled = locationPermission.hasPermission

    userLocationLayer.setObjectListener(object : UserLocationObjectListener {
        override fun onObjectAdded(userLocationView: UserLocationView) {
            if (userLocation){
                setUserLocation(
                    mapView = mapView,
                    userLocationLayer = userLocationLayer
                )
            }
        }

        override fun onObjectRemoved(userLocationView: UserLocationView) {}

        override fun onObjectUpdated(userLocationView: UserLocationView, objectEvent: ObjectEvent) {}
    })

    SearchFactory.initialize(context)

    mapView.map.addCameraListener { map, cameraPosition, cameraUpdateReason, finished ->

        onCameraPosition(cameraPosition)

        if (finished){
            search?.let {
                if (search.isNotEmpty()){
                    submitQuery(
                        context = context,
                        query = search,
                        searchManager = searchManager,
                        mapView = mapView
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = search, block = {
        search?.let {
            if (search.isNotEmpty()){
                submitQuery(
                    context = context,
                    query = search,
                    searchManager = searchManager,
                    mapView = mapView
                )
            }
        }
    })

    LaunchedEffect(key1 = userLocation, block = {
        if (userLocation){
            if (locationPermission.hasPermission){
                setUserLocation(
                    mapView = mapView,
                    userLocationLayer = userLocationLayer
                )
            }else{
                locationPermission.launchPermissionRequest()
            }
        }
    })

    LaunchedEffect(key1 = cameraPosition, block = {
        mapView.map.move(
            cameraPosition,
            Animation(Animation.Type.SMOOTH,1f),
            null
        )
    })

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

    YandexMapView(
        modifier = modifier,
        mapView = mapView
    )
}

@Composable
private fun YandexMapView(
    modifier: Modifier,
    mapView: MapView,
){
    AndroidView(modifier = modifier, factory = {
        mapView.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    })
}

private fun setUserLocation(
    mapView: MapView,
    userLocationLayer:UserLocationLayer
){
    userLocationLayer.setAnchor(
        PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
        PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
    )
}

private fun submitQuery(
    context: Context,
    query: String,
    searchManager: SearchManager,
    mapView: MapView
){
    searchManager.submit(
        query,
        VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
        SearchOptions(),
        object : Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                val mapObjects = mapView.map.mapObjects
                mapObjects.clear()

                for (searchResult in response.collection.children) {
                    val resultLocation = searchResult.obj!!.geometry[0].point
                    if (resultLocation != null) {
                        mapObjects.addPlacemark(
                            resultLocation,
                            ImageProvider.fromResource(context,
                                R.drawable.search_result
                            )
                        )
                    }
                }
            }

            override fun onSearchError(error: Error) {
                Toast.makeText(
                    context,
                    error.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}