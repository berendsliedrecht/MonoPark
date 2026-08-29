package com.berend.parking

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson

/** SMSParking short code: "<zone> <plate>" to start, "Q" to stop. */
const val SMS_NUMBER = "4030"

/** A saved vehicle. The plate is what gets texted to the parking service. */
data class Car(val label: String, val plate: String)

/** A locally tracked running session; the authoritative one lives at the provider. */
data class ActiveSession(val zone: String, val plate: String, val startedAt: Long)

class ParkingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("park", Context.MODE_PRIVATE)
    private val gson = Gson()

    var cars by mutableStateOf<List<Car>>(emptyList()); private set
    var selectedPlate by mutableStateOf<String?>(null); private set
    var session by mutableStateOf<ActiveSession?>(null); private set

    /** Zone code the user is typing for a new session. */
    var zone by mutableStateOf("")

    init {
        cars = runCatching {
            gson.fromJson(prefs.getString("cars", null), Array<Car>::class.java)?.toList()
        }.getOrNull().orEmpty()
        session = runCatching {
            gson.fromJson(prefs.getString("session", null), ActiveSession::class.java)
        }.getOrNull()
        selectedPlate = cars.firstOrNull { it.plate == prefs.getString("selected_plate", null) }?.plate
            ?: cars.firstOrNull()?.plate
    }

    val selectedCar: Car? get() = cars.firstOrNull { it.plate == selectedPlate }

    fun addCar(label: String, plate: String) {
        val normalized = plate.filter { it.isLetterOrDigit() }.uppercase()
        if (normalized.isBlank()) return
        val car = Car(label.trim().ifBlank { normalized }, normalized)
        cars = cars.filterNot { it.plate == normalized } + car
        if (selectedPlate == null) selectCar(normalized)
        prefs.edit().putString("cars", gson.toJson(cars)).apply()
    }

    fun removeCar(car: Car) {
        cars = cars.filterNot { it.plate == car.plate }
        if (selectedPlate == car.plate) selectCar(cars.firstOrNull()?.plate)
        prefs.edit().putString("cars", gson.toJson(cars)).apply()
    }

    fun selectCar(plate: String?) {
        selectedPlate = plate
        prefs.edit().putString("selected_plate", plate).apply()
    }

    /** SMS body to start a session: "<zone> <plate>". */
    fun startMessage(zone: String, plate: String): String = "${zone.trim()} $plate"

    /** SMS body to stop the running session. */
    fun stopMessage(): String = "Q"

    fun markStarted(zone: String, plate: String) {
        session = ActiveSession(zone.trim(), plate, System.currentTimeMillis())
        this.zone = ""
        prefs.edit().putString("session", gson.toJson(session)).apply()
    }

    fun markStopped() {
        session = null
        prefs.edit().remove("session").apply()
    }
}
