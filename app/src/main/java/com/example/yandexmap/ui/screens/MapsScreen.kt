package com.example.yandexmap.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.yandexmap.ui.view.YandexMap
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@ExperimentalPermissionsApi
@Composable
fun MapsScreen() {
    YandexMap(
        modifier = Modifier.fillMaxSize()
    )
}