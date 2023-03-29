package com.example.yandexmap.ui.screens

import android.Manifest
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.yandexmap.ui.theme.tintColor
import com.example.yandexmap.ui.view.YandexMap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.example.yandexmap.R
import com.example.yandexmap.ui.view.rememberCameraPosition
import com.example.yandexmap.utils.getUserLocation
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@ExperimentalMaterialApi
@ExperimentalPermissionsApi
@Composable
fun MapsScreen() {
    val context = LocalContext.current

    var userLocation by rememberSaveable { mutableStateOf(true) }
    var cameraPosition = rememberCameraPosition()

    val locationPermission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    var search by rememberSaveable{ mutableStateOf("") }

    if (userLocation){
        if(locationPermission.hasPermission){
            val userPoint = getUserLocation(context)
            cameraPosition = rememberCameraPosition(
                longitude = userPoint?.longitude ?: 0.0,
                latitude = userPoint?.latitude ?: 0.0,
                zoom = 14f
            )
        }
    }

    LaunchedEffect(key1 = Unit, block = {
        delay(10000)
        val userPoint = getUserLocation(context)
        cameraPosition = rememberCameraPosition(
            longitude = userPoint?.longitude ?: 0.0,
            latitude = userPoint?.latitude ?: 0.0,
            zoom = 14f
        )
    })

    Box {
        YandexMap(
            modifier = Modifier.fillMaxSize(),
            cameraPosition = cameraPosition,
            userLocation = true,
            search = search,
            onCameraPosition = { newCameraPosition ->
                cameraPosition = newCameraPosition
            }
        )
        Column {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .padding(5.dp),
                    value = search,
                    onValueChange = { search = it },
                    shape = AbsoluteRoundedCornerShape(15.dp),
                    label = { Text(text = "Search") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.Black
                    )
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {

                MapButton(
                    iconId = R.drawable.ic_zoom_in
                ) {
                    cameraPosition = rememberCameraPosition(
                        cameraPosition = cameraPosition,
                        zoom = cameraPosition.zoom + 5f
                    )
                }

                MapButton(
                    iconId = R.drawable.ic_zoom_out
                ) {
                    cameraPosition = rememberCameraPosition(
                        cameraPosition = cameraPosition,
                        zoom = cameraPosition.zoom - 5f
                    )
                }

                MapButton(
                    iconId = R.drawable.ic_near_me,
                    tint = if (userLocation) Color.Blue else Color.White
                ) {
                    userLocation = !userLocation
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun MapButton(
    @DrawableRes iconId: Int,
    tint:Color = Color.White,
    onClick:() -> Unit
){
    Card(
        modifier = Modifier.padding(5.dp),
        shape = AbsoluteRoundedCornerShape(20.dp),
        backgroundColor = tintColor,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier
                .padding(15.dp),
            painter = painterResource(id = iconId),
            contentDescription = null,
            tint = tint
        )
    }
}