package com.example.multiplatform.data

import com.example.multiplatform.config.BackendConfig
import com.example.multiplatform.model.ChatMessage
import com.example.multiplatform.model.ImageUploadTarget
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class HttpChatBackend(
    private val baseUrl: String = BackendConfig.defaultBaseUrl,
    private val client: HttpClient = defaultChatHttpClient(),
) : ChatBackend {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getHistory(threadId: String): List<ChatMessage> {
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
        val response = client.put(target.uploadUrl) {
            contentType(ContentType.parse(target.contentType))
            setBody(bytes)
        }
        if (!response.status.isSuccess()) {
            val detail = response.bodyAsText().take(200)
            throw IllegalStateException("图片上传失败: ${response.status.value} $detail")
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

        client.preparePost("$baseUrl/api/v1/chat/stream") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.execute { response ->
            val channel = response.bodyAsChannel()
            val buffer = ByteArray(2048)
            while (!channel.isClosedForRead) {
                val read = channel.readAvailable(buffer)
                if (read > 0) {
                    onChunk(buffer.decodeToString(0, read))
                }
            }
        }
    }

    private fun parseMessages(body: String): List<ChatMessage> {
        val root = json.parseToJsonElement(body).jsonObject
        val messages = root["messages"]?.jsonArray ?: return emptyList()

        return messages.mapNotNull { item ->
            val message = item.jsonObject
            val role = message["role"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val content = message["content"]?.jsonPrimitive?.content ?: return@mapNotNull null

            ChatMessage(
                isUser = role == "user",
                content = content,
            )
        }
    }

    private fun String.trimQuotes(): String = trim().trim('"', '\'')
}

private fun defaultChatHttpClient(): HttpClient = HttpClient {
    install(HttpTimeout) {
        connectTimeoutMillis = BackendConfig.connectTimeoutMillis
        requestTimeoutMillis = BackendConfig.chatRequestTimeoutMillis
        socketTimeoutMillis = BackendConfig.chatRequestTimeoutMillis
    }
}
