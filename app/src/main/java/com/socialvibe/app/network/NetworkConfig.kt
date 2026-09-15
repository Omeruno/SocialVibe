package com.socialvibe.app.network

object NetworkConfig {
    // 10.0.2.2 is the Android emulator's alias for the host machine's
    // localhost — this only works when the backend is running on the same
    // computer as Android Studio. Point this at a real server's URL (https,
    // ideally) once the backend is deployed.
    const val BASE_URL = "http://10.0.2.2:3000"
}
