package com.example.yandexmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yandexmap.ui.navigation.Screens
import com.example.yandexmap.ui.screens.MainScreen
import com.example.yandexmap.ui.screens.MapsScreen
import com.example.yandexmap.ui.screens.PanoramaMapsScreen
import com.example.yandexmap.ui.theme.YandexMapTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val navController = rememberNavController()

            YandexMapTheme {
                NavHost(
                    navController = navController,
                    startDestination = Screens.MainScreen.route,
                    builder = {
                        composable(Screens.MainScreen.route){
                            MainScreen(
                                navController = navController
                            )
                        }

                        composable(Screens.MapsScreen.route){
                            MapsScreen()
                        }

                        composable(Screens.PanoramaMapsScreen.route){
                            PanoramaMapsScreen()
                        }
                    }
                )
            }
        }
    }
}