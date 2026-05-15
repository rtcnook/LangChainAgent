package com.example.multiplatform.platform

actual class AppClipboard {
    actual fun copy(text: String) = Unit
    actual fun readText(): String? = null
}
