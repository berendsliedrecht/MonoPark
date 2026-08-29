package com.berend.parking

import android.content.Context
import android.os.Build
import android.telephony.SmsManager

/**
 * Sends a text in the background, no messaging app involved. Requires the SEND_SMS
 * permission. Parking short codes may still trigger a system confirmation dialog that
 * the app cannot suppress; the user can choose "always allow" there.
 */
fun sendSms(context: Context, number: String, message: String) {
    val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java)
    } else {
        @Suppress("DEPRECATION")
        SmsManager.getDefault()
    }
    manager.sendTextMessage(number, null, message, null, null)
}
