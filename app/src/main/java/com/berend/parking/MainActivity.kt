package com.berend.parking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.berend.parking.ui.CarsScreen
import com.berend.parking.ui.ParkScreen
import com.mudita.mmd.ThemeMMD

enum class Screen { Park, Cars }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemeMMD {
                ParkApp()
            }
        }
    }
}

@Composable
fun ParkApp(viewModel: ParkingViewModel = viewModel()) {
    var screen by remember { mutableStateOf(Screen.Park) }

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        when (screen) {
            Screen.Park -> ParkScreen(
                viewModel = viewModel,
                onOpenCars = { screen = Screen.Cars },
            )
            Screen.Cars -> CarsScreen(viewModel = viewModel, onBack = { screen = Screen.Park })
        }
    }
}
