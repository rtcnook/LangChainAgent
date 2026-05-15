package com.example.multiplatform.data

import com.example.multiplatform.model.ChatMessage
import com.example.multiplatform.model.ImageUploadTarget

interface ChatBackend {
    suspend fun getHistory(threadId: String): List<ChatMessage>
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
