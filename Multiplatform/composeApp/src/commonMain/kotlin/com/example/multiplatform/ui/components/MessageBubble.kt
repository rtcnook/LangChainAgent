package com.example.multiplatform.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multiplatform.model.ChatMessage
import com.example.multiplatform.ui.theme.ChefGradient
import com.example.multiplatform.ui.theme.UserGradient

@Composable
fun MessageBubble(
    message: ChatMessage,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            if (!message.isUser) {
                GradientBadge(text = if (message.isStreaming && message.content.isEmpty()) "..." else "厨", brush = ChefGradient)
                Spacer(Modifier.width(10.dp))
            }
            MessageCard(message = message, onCopy = onCopy)
            if (message.isUser) {
                Spacer(Modifier.width(10.dp))
                GradientBadge(text = "你", brush = UserGradient)
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: ChatMessage,
    onCopy: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)

    if (message.isUser) {
        Column(
            modifier = Modifier
                .clip(shape)
                .background(UserGradient)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = message.content,
                color = Color.White,
                lineHeight = 21.sp,
                fontSize = 14.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CopyTextButton(color = Color.White.copy(alpha = 0.9f), text = "复制", onClick = onCopy)
            }
        }
    } else {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = if (message.isStreaming && message.content.isEmpty()) "⏳ AI 正在思考中..." else message.content,
                    color = Color(0xFF1F2937),
                    lineHeight = 21.sp,
                    fontSize = 14.sp,
                )
                if (message.content.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CopyTextButton(color = Color(0xFFF97316), text = "复制", onClick = onCopy)
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyTextButton(color: Color, text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 2.dp),
    )
}
