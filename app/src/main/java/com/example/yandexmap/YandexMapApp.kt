package com.example.yandexmap

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class YandexMapApp:Application() {

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("e7534c10-cf8d-44a3-be67-3399d2210add")
        MapKitFactory.initialize(this)
    }
}