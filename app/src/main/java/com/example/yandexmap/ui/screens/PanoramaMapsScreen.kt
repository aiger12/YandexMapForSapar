package com.example.yandexmap.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.yandexmap.ui.view.PanoramaMap

@Composable
fun PanoramaMapsScreen() {
    PanoramaMap(
        modifier = Modifier.fillMaxSize()
    )
}