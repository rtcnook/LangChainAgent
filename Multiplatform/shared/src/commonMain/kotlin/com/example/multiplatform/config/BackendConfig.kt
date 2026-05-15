package com.example.multiplatform.config

object BackendConfig {
    const val defaultBaseUrl = "http://192.168.2.2:8001"
    const val connectTimeoutMillis: Long = 60_000
    val chatRequestTimeoutMillis: Long = 600_000
    val imageUploadTimeoutMillis: Long = 60_000L
}
