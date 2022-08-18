package com.example.yandexmap.ui.view

import android.Manifest
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.*
import android.graphics.Paint.Align
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
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
import com.example.yandexmap.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import kotlin.math.abs
import kotlin.math.sqrt

fun rememberCameraPosition(
    cameraPosition: CameraPosition? = null,
    latitude: Double = cameraPosition?.target?.latitude ?: 0.0,
    longitude: Double = cameraPosition?.target?.longitude ?: 0.0,
    zoom: Float = cameraPosition?.zoom ?: 0f,
    azimuth: Float = cameraPosition?.azimuth ?: 0f,
    tilt: Float = cameraPosition?.tilt ?: 0f,
):CameraPosition {
    Log.e("zoom", zoom.toString())

    return CameraPosition(
        Point(
            latitude,
            longitude
        ),
        if (zoom < 0) 0f else zoom,
        azimuth,
        tilt
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

    val clusterizedCollection = mapView.map.mapObjects.addClusterizedPlacemarkCollection { cluster ->
        cluster.appearance.setIcon(
            TextImageProvider(cluster.size.toString(),context)
        )
    }

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

    mapView.map.addCameraListener { map, cameraPosition, cameraUpdateReason, finished ->

        onCameraPosition(cameraPosition)

        if (finished){
            search?.let {
                if (search.isNotEmpty()){
                    submitQuery(
                        context = context,
                        query = search,
                        searchManager = searchManager,
                        mapView = mapView,
                        clusterizedCollection = clusterizedCollection
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit, block = {
        SearchFactory.initialize(context)
    })

    LaunchedEffect(key1 = search, block = {
        search?.let {
            if (search.isNotEmpty()){
                submitQuery(
                    context = context,
                    query = search,
                    searchManager = searchManager,
                    mapView = mapView,
                    clusterizedCollection = clusterizedCollection
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

private class TextImageProvider(
    private val text: String,
    private val context: Context,
) : ImageProvider() {

    companion object {
        const val FONT_SIZE = 15f
        const val MARGIN_SIZE = 3f
        const val STROKE_SIZE = 3f
    }

    override fun getId(): String  = "text_$text"

    override fun getImage(): Bitmap {

        val metrics = DisplayMetrics()
        val manager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getMetrics(metrics)

        val textPaint = Paint()
        textPaint.textSize = FONT_SIZE * metrics.density
        textPaint.textAlign = Align.CENTER
        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true

        val widthF = textPaint.measureText(text)
        val textMetrics = textPaint.fontMetrics
        val heightF = abs(textMetrics.bottom) + abs(textMetrics.top)
        val textRadius = sqrt(x = (widthF * widthF + heightF * heightF).toDouble()).toFloat() / 2
        val internalRadius: Float =
            textRadius + MARGIN_SIZE * metrics.density
        val externalRadius: Float =
            internalRadius + STROKE_SIZE * metrics.density

        val width = (2 * externalRadius + 0.5).toInt()

        val bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val backgroundPaint = Paint()
        backgroundPaint.isAntiAlias = true
        backgroundPaint.color = Color.RED
        canvas.drawCircle((width / 2).toFloat(),
            (width / 2).toFloat(),
            externalRadius,
            backgroundPaint
        )

        backgroundPaint.color = Color.WHITE
        canvas.drawCircle((width / 2).toFloat(),
            (width / 2).toFloat(),
            internalRadius,
            backgroundPaint
        )

        canvas.drawText(
            text, (width / 2).toFloat(),
            width / 2 - (textMetrics.ascent + textMetrics.descent) / 2,
            textPaint
        )

        return bitmap
    }
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
    userLocationLayer: UserLocationLayer,
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
    mapView: MapView,
    clusterizedCollection: ClusterizedPlacemarkCollection,
){
    searchManager.submit(
        query,
        VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
        SearchOptions(),
        object : Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                clusterizedCollection.clear()

                for (searchResult in response.collection.children) {
                    val resultLocation = searchResult.obj!!.geometry[0].point
                    if (resultLocation != null) {
                        clusterizedCollection.addPlacemark(
                            resultLocation,
                            ImageProvider.fromResource(context,
                                R.drawable.search_result
                            )
                        )
                    }
                }

                clusterizedCollection.clusterPlacemarks(60.0, 15)
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