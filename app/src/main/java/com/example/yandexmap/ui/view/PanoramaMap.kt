package com.example.yandexmap.ui.view

import android.view.ViewGroup
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
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.mapkit.places.panorama.PanoramaView
import com.yandex.runtime.Error

@Composable
fun PanoramaMap(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val panoramaView = remember { PanoramaView(context) }

    val mapKit = remember { MapKitFactory.getInstance() }

    val panoramaService = PlacesFactory.getInstance().createPanoramaService()
    panoramaService.findNearest(
        Point(43.2415566, 76.908833),
        object: PanoramaService.SearchListener {
            override fun onPanoramaSearchResult(panoramaId: String) {
                panoramaView.player.openPanorama(panoramaId)
                panoramaView.player.enableMove()
                panoramaView.player.enableRotation()
                panoramaView.player.enableZoom()
                panoramaView.player.enableMarkers()
            }

            override fun onPanoramaSearchError(error: Error) {

            }
        }
    )

    LaunchedEffect(key1 = Unit, block = {
        PlacesFactory.initialize(context)
    })

    DisposableEffect(key1 = lifecycleOwner, effect = {

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START){
                panoramaView.onStart()
                mapKit.onStart()
            }
            if (event == Lifecycle.Event.ON_STOP){
                panoramaView.onStop()
                mapKit.onStop()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    AndroidView(modifier = modifier,factory = {
        panoramaView.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    })
}