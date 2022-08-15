package com.example.yandexmap.ui.navigation

sealed class Screens(val route:String){
    object MainScreen:Screens("main_screen")
    object MapsScreen:Screens("maps_screen")
    object PanoramaMapsScreen:Screens("panorama_maps_screen")
}
