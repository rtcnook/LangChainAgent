package com.example.multiplatform.model

data class ChatMessage(
    val isUser: Boolean,
    val content: String,
    val isStreaming: Boolean = false,
)
