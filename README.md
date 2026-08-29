# Park

Minimal SMS parking app for the Mudita Kompakt (e-ink), built with the Mudita Mindful Design (MMD) framework. Type a zone code, pick a vehicle, and start a paid-parking session by SMS to [SMSParking](https://smsparking.nl). While a session runs, the app shows a live timer.

The app does **not** handle payment: it only sends SMSParking's SMS commands, and billing happens on your pre-registered SMSParking account. You need an account first.

## How it works

- **Start**: sends `<zone> <plate>` to `4030`.
- **Stop**: sends `Q` to `4030`.
- Sending uses Android's `SEND_SMS` permission, so no messaging app opens. A parking short code may still trigger a one-time system confirmation you can set to "always allow".

## Install

```
./gradlew installDebug
```

Or grab the APK from [Releases](../../releases) and sideload it.

## Structure

- `ParkingViewModel.kt`: vehicles and active-session state, persisted; the `SMS_NUMBER` constant
- `Sms.kt`: background SMS send via `SmsManager`
- `ui/ParkScreen.kt`: zone input, vehicle picker, start/stop with a send-confirmation dialog, and the running-session timer
- `ui/CarsScreen.kt`: add and remove vehicles

## Notes

- Needs a real SMSParking account and a SIM that can send SMS.
- Session tracking is local (the authoritative session lives at SMSParking), so the timer reflects when you started via this app.

## License

[MIT](LICENSE)
