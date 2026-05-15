package com.example.multiplatform.platform

expect class AppClipboard() {
    fun copy(text: String)
    fun readText(): String?
}
