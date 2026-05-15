package com.example.multiplatform.domain

import com.example.multiplatform.data.ChatBackend
import com.example.multiplatform.model.ChatMessage
import com.example.multiplatform.model.SelectedImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatController(
    private val backend: ChatBackend,
    private val threadId: String = "default-thread",
    private val uploadTimestampProvider: () -> Long = { currentTimeMillis() },
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messagesFlow: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _processing = MutableStateFlow(false)
    val processingFlow: StateFlow<Boolean> = _processing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessageFlow: StateFlow<String?> = _errorMessage.asStateFlow()

    val messages: List<ChatMessage>
        get() = _messages.value

    val processing: Boolean
        get() = _processing.value

    suspend fun loadHistory() {
        runCatching {
            backend.getHistory(threadId)
        }.onSuccess { history ->
            _messages.value = history
            _errorMessage.value = null
        }.onFailure { error ->
            _errorMessage.value = error.message ?: "加载历史消息失败"
        }
    }

    suspend fun newChat() {
        runCatching {
            backend.clearHistory(threadId)
        }.onSuccess {
            _messages.value = emptyList()
            _errorMessage.value = null
        }.onFailure { error ->
            _errorMessage.value = error.message ?: "清空历史失败"
        }
    }

    suspend fun sendMessage(text: String, image: SelectedImage? = null) {
        val trimmed = text.trim()
        if ((trimmed.isEmpty() && image == null) || _processing.value) return

        val userContent = if (image == null) {
            trimmed
        } else {
            "$trimmed\n[图片]: ${image.fileName}".trim()
        }
        val userMessage = ChatMessage(isUser = true, content = userContent)
        val assistantIndex = _messages.value.size + 1
        _messages.value = _messages.value + userMessage + ChatMessage(
            isUser = false,
            content = "",
            isStreaming = true,
        )
        _processing.value = true
        _errorMessage.value = null

        runCatching {
            val imageUrl = image?.let { selected ->
                val uploadTarget = backend.presignImageUpload(selected.toOssFileName())
                backend.uploadImage(uploadTarget, selected.bytes)
                uploadTarget.accessUrl
            }
            backend.streamChat(threadId, trimmed.ifEmpty { "请识别这张图片" }, imageUrl) { chunk ->
                appendAssistantChunk(assistantIndex, chunk)
            }
        }.onFailure { error ->
            appendAssistantChunk(assistantIndex, "\n[错误]: ${error.message ?: "Failed to fetch"}")
            _errorMessage.value = error.message ?: "发送失败"
        }

        _messages.value = _messages.value.mapIndexed { index, message ->
            if (index == assistantIndex) message.copy(isStreaming = false) else message
        }
        _processing.value = false
    }

    private fun appendAssistantChunk(index: Int, chunk: String) {
        _messages.value = _messages.value.mapIndexed { messageIndex, message ->
            if (messageIndex == index) {
                message.copy(content = message.content + chunk)
            } else {
                message
            }
        }
    }

    private fun SelectedImage.toOssFileName(): String {
        val extension = fileName.substringAfterLast('.', "jpg")
            .lowercase()
            .ifBlank { "jpg" }
        return "${uploadTimestampProvider()}.$extension"
    }
}
