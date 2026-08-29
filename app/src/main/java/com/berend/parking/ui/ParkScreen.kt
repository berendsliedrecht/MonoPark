package com.berend.parking.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.berend.parking.Car
import com.berend.parking.ParkingViewModel
import com.berend.parking.SMS_NUMBER
import com.berend.parking.sendSms
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Confirm { Start, Stop }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkScreen(
    viewModel: ParkingViewModel,
    onOpenCars: () -> Unit,
) {
    val context = LocalContext.current
    var confirm by remember { mutableStateOf<Confirm?>(null) }

    // Runs a send once SEND_SMS is granted; the pending block is captured on request.
    var pendingSend by remember { mutableStateOf<(() -> Unit)?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) pendingSend?.invoke()
        else Toast.makeText(context, "SMS permission needed to send", Toast.LENGTH_SHORT).show()
        pendingSend = null
    }

    fun withPermission(sendAction: () -> Unit) {
        val granted = context.checkSelfPermission(Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) sendAction()
        else {
            pendingSend = sendAction
            permissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    fun send(message: String, onSent: () -> Unit) = withPermission {
        runCatching { sendSms(context, SMS_NUMBER, message) }
            .onSuccess { onSent() }
            .onFailure { Toast.makeText(context, "Sending failed: ${it.message}", Toast.LENGTH_LONG).show() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBarMMD(
            title = { TextMMD("Park", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = onOpenCars) {
                    Icon(Icons.Outlined.DirectionsCar, contentDescription = "Cars")
                }
            },
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            val session = viewModel.session
            if (session != null) {
                ActiveSessionView(
                    zone = session.zone,
                    plate = session.plate,
                    startedAt = session.startedAt,
                    onStopClick = { confirm = Confirm.Stop },
                )
            } else {
                StartSessionView(
                    viewModel = viewModel,
                    onStartClick = { confirm = Confirm.Start },
                    onOpenCars = onOpenCars,
                )
            }
        }
    }

    when (confirm) {
        Confirm.Start -> {
            val car = viewModel.selectedCar
            ConfirmDialog(
                message = "Send “${viewModel.startMessage(viewModel.zone, car?.plate.orEmpty())}” to $SMS_NUMBER?",
                onConfirm = {
                    if (car != null) {
                        val zone = viewModel.zone
                        send(viewModel.startMessage(zone, car.plate)) { viewModel.markStarted(zone, car.plate) }
                    }
                    confirm = null
                },
                onDismiss = { confirm = null },
            )
        }

        Confirm.Stop -> ConfirmDialog(
            message = "Send “Q” to $SMS_NUMBER to stop?",
            onConfirm = {
                send(viewModel.stopMessage()) { viewModel.markStopped() }
                confirm = null
            },
            onDismiss = { confirm = null },
        )

        null -> Unit
    }
}

@Composable
private fun ActiveSessionView(
    zone: String,
    plate: String,
    startedAt: Long,
    onStopClick: () -> Unit,
) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val clock = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Spacer(modifier = Modifier.height(24.dp))
    TextMMD("Parking active", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    TextMMD("Zone $zone  ·  $plate", fontSize = 16.sp)
    TextMMD("Started ${clock.format(Date(startedAt))}", fontSize = 14.sp)

    Spacer(modifier = Modifier.height(24.dp))
    TextMMD(
        text = elapsed(now - startedAt),
        fontSize = 56.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(32.dp))

    BlackButton(text = "Stop parking", onClick = onStopClick)
}

@Composable
private fun StartSessionView(
    viewModel: ParkingViewModel,
    onStartClick: () -> Unit,
    onOpenCars: () -> Unit,
) {
    Spacer(modifier = Modifier.height(16.dp))
    TextMMD("Zone code", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(4.dp))
    com.mudita.mmd.components.text_field.TextFieldMMD(
        value = viewModel.zone,
        onValueChange = { viewModel.zone = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { TextMMD("e.g. 12345") },
    )

    Spacer(modifier = Modifier.height(16.dp))
    TextMMD("Vehicle", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(4.dp))
    if (viewModel.cars.isEmpty()) {
        OutlinedButtonMMD(onClick = onOpenCars, modifier = Modifier.fillMaxWidth()) {
            TextMMD("Add a vehicle", fontSize = 16.sp)
        }
    } else {
        viewModel.cars.forEach { car ->
            CarChoice(
                car = car,
                selected = car.plate == viewModel.selectedPlate,
                onClick = { viewModel.selectCar(car.plate) },
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    val ready = viewModel.zone.isNotBlank() && viewModel.selectedCar != null
    BlackButton(text = "Start parking", onClick = onStartClick, enabled = ready)
}

@Composable
private fun CarChoice(car: Car, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) BorderStroke(3.dp, Color.Black) else BorderStroke(1.dp, Color.Black)
    OutlinedButtonMMD(
        onClick = onClick,
        border = border,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextMMD(
                text = car.plate,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
            )
            if (car.label != car.plate) TextMMD(car.label, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ConfirmDialog(message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color.White, border = BorderStroke(2.dp, Color.Black)) {
            Column(modifier = Modifier.padding(20.dp)) {
                TextMMD(message, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BlackButton(text = "Send", onClick = onConfirm, modifier = Modifier.weight(1f))
                    OutlinedButtonMMD(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        TextMMD("Cancel", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BlackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
) {
    ButtonMMD(
        onClick = onClick,
        enabled = enabled,
        // Pure black/white so e-ink doesn't dither the fill
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
            disabledContainerColor = Color.White,
            disabledContentColor = Color.Black,
        ),
        border = BorderStroke(2.dp, Color.Black),
        modifier = modifier,
    ) {
        TextMMD(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

private fun elapsed(millis: Long): String {
    val total = (millis / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return "%d:%02d:%02d".format(h, m, s)
}
