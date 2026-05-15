package com.example.multiplatform.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val WarmBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF7ED),
        Color(0xFFFFFBEB),
        Color(0xFFFEF2F2),
    ),
)

val ChefGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF97316), Color(0xFFEF4444)),
)

val UserGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
)
