package com.example.multiplatform.ui.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import com.example.multiplatform.model.ChatMessage
import com.example.multiplatform.model.SelectedImage

data class ChatUiState(
    val draft: TextFieldValue = TextFieldValue(""),
    val selectedImage: SelectedImage? = null,
    val messages: List<ChatMessage> = emptyList(),
    val processing: Boolean = false,
    val errorMessage: String? = null,
)
