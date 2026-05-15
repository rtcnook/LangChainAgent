package com.example.multiplatform.ui.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import com.example.multiplatform.data.HttpChatBackend
import com.example.multiplatform.domain.ChatController
import com.example.multiplatform.model.SelectedImage
import com.example.multiplatform.ui.components.insertText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val controller: ChatController = ChatController(HttpChatBackend()),
    initialDraft: TextFieldValue = TextFieldValue(""),
) {
    private val _uiState = MutableStateFlow(ChatUiState(draft = initialDraft))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun updateDraft(value: TextFieldValue) {
        _uiState.value = _uiState.value.copy(draft = value)
    }

    fun pasteIntoDraft(text: String) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.insertText(text),
        )
    }

    fun selectImage(image: SelectedImage) {
        _uiState.value = _uiState.value.copy(selectedImage = image)
    }

    fun clearImage() {
        _uiState.value = _uiState.value.copy(selectedImage = null)
    }

    fun bind(scope: CoroutineScope) {
        scope.launch {
            controller.messagesFlow.collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
        scope.launch {
            controller.processingFlow.collect { processing ->
                _uiState.value = _uiState.value.copy(processing = processing)
            }
        }
        scope.launch {
            controller.errorMessageFlow.collect { errorMessage ->
                _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
            }
        }
    }

    suspend fun loadHistory() {
        controller.loadHistory()
    }

    suspend fun newChat() {
        controller.newChat()
    }

    suspend fun sendMessage() {
        val state = _uiState.value
        if (state.draft.text.isBlank() && state.selectedImage == null) return

        val message = state.draft.text.ifBlank { "请识别这张图片" }
        val image = state.selectedImage
        _uiState.value = state.copy(
            draft = TextFieldValue(""),
            selectedImage = null,
        )
        controller.sendMessage(message, image)
    }
}
