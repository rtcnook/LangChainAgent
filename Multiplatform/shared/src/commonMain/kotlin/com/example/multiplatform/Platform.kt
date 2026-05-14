package com.example.multiplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform