package com.example.multiplatform.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.multiplatform.model.ChatMessage

@Composable
fun ChatPanel(
    messages: List<ChatMessage>,
    errorMessage: String?,
    onCopyMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (messages.isEmpty()) {
            EmptyState(errorMessage)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                messages.forEach { message ->
                    MessageBubble(
                        message = message,
                        onCopy = { onCopyMessage(message.content) },
                    )
                }
                if (!errorMessage.isNullOrBlank()) {
                    StatusText(errorMessage)
                }
            }
        }
    }
}
