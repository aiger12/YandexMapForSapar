package com.example.yandexmap

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class YandexMapApp:Application() {

    override fun onCreate() {
        super.onCreate()
        val mapKey = BuildConfig.YANDEX_MAP_KIT_KEY

        MapKitFactory.setApiKey(mapKey)
        MapKitFactory.initialize(this)
    }
}