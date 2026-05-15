package com.example.multiplatform

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ChatControllerTest {

    @Test
    fun defaultBackendBaseUrlUsesComputerLanIp() {
        assertEquals("http://192.168.2.2:8001", BackendConfig.defaultBaseUrl)
    }

    @Test
    fun loadHistoryReplacesMessagesFromBackend() = runBlocking {
        val backend = FakeChatBackend(
            history = listOf(ChatPreviewMessage(isUser = false, content = "历史推荐"))
        )
        val controller = ChatController(backend)

        controller.loadHistory()

        assertEquals(listOf(ChatPreviewMessage(isUser = false, content = "历史推荐")), controller.messages)
        assertEquals("default-thread", backend.loadedThreadId)
    }

    @Test
    fun sendMessageAppendsUserMessageAndAssistantResponse() = runBlocking {
        val backend = FakeChatBackend(responseChunks = listOf("推荐", "番茄炒蛋"))
        val controller = ChatController(backend)

        controller.sendMessage("我有番茄和鸡蛋")

        assertFalse(controller.processing)
        assertEquals(2, controller.messages.size)
        assertEquals(ChatPreviewMessage(isUser = true, content = "我有番茄和鸡蛋"), controller.messages[0])
        assertEquals(ChatPreviewMessage(isUser = false, content = "推荐番茄炒蛋"), controller.messages[1])
        assertEquals("我有番茄和鸡蛋", backend.sentMessage)
    }

    @Test
    fun sendMessageUploadsSelectedImageAndPassesAccessUrl() = runBlocking {
        val backend = FakeChatBackend(responseChunks = listOf("看起来可以做番茄炒蛋"))
        val controller = ChatController(backend, uploadTimestampProvider = { 1700000000000L })
        val image = SelectedImage(
            fileName = "我的 番茄(1).PNG",
            bytes = byteArrayOf(1, 2, 3),
        )

        controller.sendMessage("识别这张图", image)

        assertEquals("1700000000000.png", backend.presignedFileName)
        assertEquals(byteArrayOf(1, 2, 3).toList(), backend.uploadedBytes?.toList())
        assertEquals("image/png", backend.uploadedContentType)
        assertEquals("https://example.com/1700000000000.png", backend.sentImageUrl)
        assertEquals(ChatPreviewMessage(isUser = true, content = "识别这张图\n[图片]: 我的 番茄(1).PNG"), controller.messages[0])
    }

    @Test
    fun newChatClearsLocalAndRemoteHistory() = runBlocking {
        val backend = FakeChatBackend(
            history = listOf(ChatPreviewMessage(isUser = true, content = "旧消息"))
        )
        val controller = ChatController(backend)
        controller.loadHistory()

        controller.newChat()

        assertEquals(emptyList(), controller.messages)
        assertEquals("default-thread", backend.clearedThreadId)
    }
}

private class FakeChatBackend(
    private val history: List<ChatPreviewMessage> = emptyList(),
    private val responseChunks: List<String> = emptyList(),
) : ChatBackend {
    var loadedThreadId: String? = null
    var clearedThreadId: String? = null
    var sentMessage: String? = null
    var sentImageUrl: String? = null
    var presignedFileName: String? = null
    var uploadedBytes: ByteArray? = null
    var uploadedContentType: String? = null

    override suspend fun getHistory(threadId: String): List<ChatPreviewMessage> {
        loadedThreadId = threadId
        return history
    }

    override suspend fun clearHistory(threadId: String) {
        clearedThreadId = threadId
    }

    override suspend fun streamChat(
        threadId: String,
        message: String,
        imageUrl: String?,
        onChunk: (String) -> Unit,
    ) {
        sentMessage = message
        sentImageUrl = imageUrl
        responseChunks.forEach(onChunk)
    }

    override suspend fun presignImageUpload(fileName: String): ImageUploadTarget {
        presignedFileName = fileName
        return ImageUploadTarget(
            uploadUrl = "https://upload.example.com/$fileName",
            contentType = "image/png",
            accessUrl = "https://example.com/$fileName",
        )
    }

    override suspend fun uploadImage(target: ImageUploadTarget, bytes: ByteArray) {
        uploadedBytes = bytes
        uploadedContentType = target.contentType
    }
}
