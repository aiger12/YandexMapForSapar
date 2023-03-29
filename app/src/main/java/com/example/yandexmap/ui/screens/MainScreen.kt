package com.example.yandexmap.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.yandexmap.ui.navigation.Screens

@Composable
fun MainScreen(
    navController: NavController
) {
    LazyColumn(
      Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp)
    ) {
        item {
            OutlinedButton(
                modifier = Modifier.padding(10.dp),
                onClick = { navController.navigate(Screens.MapsScreen.route) }
            ) {
                Text(text = "Maps")
            }

            OutlinedButton(
                modifier = Modifier.padding(10.dp),
                onClick = { navController.navigate(Screens.PanoramaMapsScreen.route) }
            ) {
                Text(text = "Panorama")
            }
        }
    }
}