package com.example.multiplatform.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multiplatform.ui.AppCopy

@Composable
fun EmptyState(errorMessage: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.82f),
            shape = CircleShape,
            shadowElevation = 4.dp,
        ) {
            Text(
                text = "食",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                color = Color(0xFFFB923C),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = AppCopy.emptyTitle,
            color = Color(0xFF4B5563),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = AppCopy.emptySubtitle,
            color = Color(0xFF9CA3AF),
            fontSize = 14.sp,
        )
        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            StatusText(errorMessage)
        }
    }
}
