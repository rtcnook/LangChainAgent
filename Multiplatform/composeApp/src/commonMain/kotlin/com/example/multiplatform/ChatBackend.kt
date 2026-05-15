package com.example.multiplatform

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class SelectedImage(
    val fileName: String,
    val bytes: ByteArray,
)

data class ImageUploadTarget(
    val uploadUrl: String,
    val contentType: String,
    val accessUrl: String,
)

expect class ImagePicker() {
    suspend fun pickImage(): SelectedImage?
}

interface ChatBackend {
    suspend fun getHistory(threadId: String): List<ChatPreviewMessage>
    suspend fun clearHistory(threadId: String)
    suspend fun presignImageUpload(fileName: String): ImageUploadTarget
    suspend fun uploadImage(target: ImageUploadTarget, bytes: ByteArray)
    suspend fun streamChat(
        threadId: String,
        message: String,
        imageUrl: String?,
        onChunk: (String) -> Unit,
    )
}

class HttpChatBackend(
    private val baseUrl: String = "http://localhost:8001",
    private val client: HttpClient = HttpClient(),
) : ChatBackend {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getHistory(threadId: String): List<ChatPreviewMessage> {
        val body = client.get("$baseUrl/api/v1/chat/messages") {
            parameter("thread_id", threadId)
        }.body<String>()

        return parseMessages(body)
    }

    override suspend fun clearHistory(threadId: String) {
        client.delete("$baseUrl/api/v1/chat/messages") {
            parameter("thread_id", threadId)
        }
    }

    override suspend fun presignImageUpload(fileName: String): ImageUploadTarget {
        val body = client.get("$baseUrl/api/v1/oss/presign") {
            parameter("filename", fileName)
        }.body<String>()

        val root = json.parseToJsonElement(body).jsonObject
        return ImageUploadTarget(
            uploadUrl = root["uploadUrl"]?.jsonPrimitive?.content?.trimQuotes() ?: "",
            contentType = root["contentType"]?.jsonPrimitive?.content ?: "application/octet-stream",
            accessUrl = root["accessUrl"]?.jsonPrimitive?.content?.trimQuotes() ?: "",
        )
    }

    override suspend fun uploadImage(target: ImageUploadTarget, bytes: ByteArray) {
        client.put(target.uploadUrl) {
            contentType(ContentType.parse(target.contentType))
            setBody(bytes)
        }
    }

    override suspend fun streamChat(
        threadId: String,
        message: String,
        imageUrl: String?,
        onChunk: (String) -> Unit,
    ) {
        val payload = buildJsonObject {
            put("message", message)
            put("thread_id", threadId)
            put("image_url", imageUrl ?: "")
        }.toString()

        val responseText = client.post("$baseUrl/api/v1/chat/stream") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body<String>()

        if (responseText.isNotBlank()) {
            onChunk(responseText)
        }
    }

    private fun parseMessages(body: String): List<ChatPreviewMessage> {
        val root = json.parseToJsonElement(body).jsonObject
        val messages = root["messages"]?.jsonArray ?: return emptyList()

        return messages.mapNotNull { item ->
            val message = item.jsonObject
            val role = message["role"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val content = message["content"]?.jsonPrimitive?.content ?: return@mapNotNull null

            ChatPreviewMessage(
                isUser = role == "user",
                content = content,
            )
        }
    }

    private fun String.trimQuotes(): String = trim().trim('"', '\'')
}

class ChatController(
    private val backend: ChatBackend,
    private val threadId: String = "default-thread",
) {
    private val _messages = MutableStateFlow<List<ChatPreviewMessage>>(emptyList())
    val messagesFlow: StateFlow<List<ChatPreviewMessage>> = _messages.asStateFlow()

    private val _processing = MutableStateFlow(false)
    val processingFlow: StateFlow<Boolean> = _processing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessageFlow: StateFlow<String?> = _errorMessage.asStateFlow()

    val messages: List<ChatPreviewMessage>
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
        val userMessage = ChatPreviewMessage(isUser = true, content = userContent)
        val assistantIndex = _messages.value.size + 1
        _messages.value = _messages.value + userMessage + ChatPreviewMessage(
            isUser = false,
            content = "",
            isStreaming = true,
        )
        _processing.value = true
        _errorMessage.value = null

        runCatching {
            val imageUrl = image?.let { selected ->
                val uploadTarget = backend.presignImageUpload(selected.fileName)
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
}
