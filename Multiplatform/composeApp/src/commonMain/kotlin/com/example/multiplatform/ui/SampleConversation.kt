package com.example.multiplatform.ui

import com.example.multiplatform.model.ChatMessage

fun sampleConversation(): List<ChatMessage> = listOf(
    ChatMessage(
        isUser = true,
        content = "我有鸡胸肉、西兰花和胡萝卜，可以做什么？",
    ),
    ChatMessage(
        isUser = false,
        content = "可以优先做鸡胸肉西兰花烤盘料理。它高蛋白、低油脂，搭配胡萝卜能补充纤维和甜味，适合快速晚餐。",
        isStreaming = true,
    ),
)
