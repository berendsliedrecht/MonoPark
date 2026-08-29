package com.berend.parking.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.berend.parking.ParkingViewModel
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.text_field.TextFieldMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarsScreen(viewModel: ParkingViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var label by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBarMMD(
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            title = { TextMMD("Vehicles", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            TextFieldMMD(
                value = plate,
                onValueChange = { plate = it.uppercase() },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { TextMMD("License plate, e.g. AB123C") },
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextFieldMMD(
                value = label,
                onValueChange = { label = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { TextMMD("Name (optional, e.g. Camper)") },
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButtonMMD(
                onClick = {
                    viewModel.addCar(label, plate)
                    label = ""
                    plate = ""
                },
                enabled = plate.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextMMD("Add vehicle", fontSize = 16.sp)
            }
        }

        // Plain scrollable column, not LazyColumnMMD: its scrollbar crashes when the
        // keyboard shrinks the viewport to a negative height. The car list is short anyway.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            viewModel.cars.forEachIndexed { index, car ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        TextMMD(car.plate, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (car.label != car.plate) TextMMD(car.label, fontSize = 13.sp)
                    }
                    IconButton(onClick = { viewModel.removeCar(car) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Remove ${car.plate}")
                    }
                }
                if (index < viewModel.cars.lastIndex) HorizontalDividerMMD()
            }
        }
    }
}
