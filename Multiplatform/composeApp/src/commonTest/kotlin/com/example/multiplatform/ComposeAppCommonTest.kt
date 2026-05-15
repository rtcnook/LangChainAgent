package com.example.multiplatform

import com.example.multiplatform.ui.AppCopy
import com.example.multiplatform.ui.sampleConversation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComposeAppCommonTest {

    @Test
    fun appCopyMatchesFrontendShell() {
        assertEquals("AI 私人厨师", AppCopy.title)
        assertEquals("上传食材图片，获取个性化食谱推荐", AppCopy.subtitle)
        assertEquals("描述你有的食材...", AppCopy.inputPlaceholder)
    }

    @Test
    fun sampleConversationContainsUserAndAssistantMessages() {
        val messages = sampleConversation()

        assertEquals(2, messages.size)
        assertTrue(messages.first().isUser)
        assertEquals("我有鸡胸肉、西兰花和胡萝卜，可以做什么？", messages.first().content)
        assertEquals(false, messages.last().isUser)
        assertTrue(messages.last().content.contains("鸡胸肉西兰花烤盘料理"))
    }
}
