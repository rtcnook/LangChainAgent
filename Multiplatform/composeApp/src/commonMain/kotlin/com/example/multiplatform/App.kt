package com.example.multiplatform

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

object AppCopy {
    const val title = "AI 私人厨师"
    const val subtitle = "上传食材图片，获取个性化食谱推荐"
    const val emptyTitle = "上传食材图片开始吧"
    const val emptySubtitle = "我会帮您识别食材并推荐食谱"
    const val inputPlaceholder = "描述你有的食材..."
}

data class ChatPreviewMessage(
    val isUser: Boolean,
    val content: String,
    val isStreaming: Boolean = false,
)

fun sampleConversation(): List<ChatPreviewMessage> = listOf(
    ChatPreviewMessage(
        isUser = true,
        content = "我有鸡胸肉、西兰花和胡萝卜，可以做什么？",
    ),
    ChatPreviewMessage(
        isUser = false,
        content = "可以优先做鸡胸肉西兰花烤盘料理。它高蛋白、低油脂，搭配胡萝卜能补充纤维和甜味，适合快速晚餐。",
        isStreaming = true,
    ),
)

private val WarmBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF7ED),
        Color(0xFFFFFBEB),
        Color(0xFFFEF2F2),
    ),
)

private val ChefGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF97316), Color(0xFFEF4444)),
)

private val UserGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        AiChefScreen()
    }
}

@Composable
private fun AiChefScreen() {
    var draft by remember { mutableStateOf("") }
    val controller = remember { ChatController(HttpChatBackend()) }
    val messages by controller.messagesFlow.collectAsState()
    val processing by controller.processingFlow.collectAsState()
    val errorMessage by controller.errorMessageFlow.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller) {
        controller.loadHistory()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeaderBar(
                onNewChat = { scope.launch { controller.newChat() } },
            )
            ChatPanel(
                messages = messages,
                errorMessage = errorMessage,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            InputBar(
                value = draft,
                onValueChange = { draft = it },
                onSend = {
                    if (draft.isNotBlank()) {
                        val message = draft
                        draft = ""
                        scope.launch {
                            controller.sendMessage(message)
                        }
                    }
                },
                enabled = !processing,
            )
        }
    }
}

@Composable
private fun HeaderBar(onNewChat: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.86f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GradientBadge(text = "厨", brush = ChefGradient)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppCopy.title,
                    color = Color(0xFFEA580C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
                Text(
                    text = AppCopy.subtitle,
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                )
            }
            Button(
                onClick = onNewChat,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("新建会话", color = Color.White)
            }
        }
    }
}

@Composable
private fun ChatPanel(
    messages: List<ChatPreviewMessage>,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.62f),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
    ) {
        if (messages.isEmpty()) {
            EmptyState(errorMessage)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                messages.forEach { message ->
                    MessageBubble(message)
                }
                if (!errorMessage.isNullOrBlank()) {
                    StatusText(errorMessage)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(errorMessage: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.82f),
            shape = CircleShape,
            shadowElevation = 4.dp,
        ) {
            Text(
                text = "食",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                color = Color(0xFFFB923C),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = AppCopy.emptyTitle,
            color = Color(0xFF4B5563),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = AppCopy.emptySubtitle,
            color = Color(0xFF9CA3AF),
            fontSize = 14.sp,
        )
        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            StatusText(errorMessage)
        }
    }
}

@Composable
private fun StatusText(text: String) {
    Text(
        text = text,
        color = Color(0xFFDC2626),
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEF2F2))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun MessageBubble(message: ChatPreviewMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            if (!message.isUser) {
                GradientBadge(text = if (message.isStreaming) "..." else "厨", brush = ChefGradient)
                Spacer(Modifier.width(10.dp))
            }
            MessageCard(message)
            if (message.isUser) {
                Spacer(Modifier.width(10.dp))
                GradientBadge(text = "你", brush = UserGradient)
            }
        }
    }
}

@Composable
private fun MessageCard(message: ChatPreviewMessage) {
    val shape = RoundedCornerShape(18.dp)

    if (message.isUser) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(UserGradient)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = message.content,
                color = Color.White,
                lineHeight = 21.sp,
                fontSize = 14.sp,
            )
        }
    } else {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = Color(0xFF1F2937),
                lineHeight = 21.sp,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun InputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.86f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextButtonLike(text = "图片")
            BasicTextField(
                value = value,
                onValueChange = { if (enabled) onValueChange(it) },
                singleLine = true,
                textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF3F4F6).copy(alpha = 0.85f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = AppCopy.inputPlaceholder,
                                color = Color(0xFF9CA3AF),
                                fontSize = 15.sp,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Button(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97316),
                    disabledContainerColor = Color(0xFFD1D5DB),
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(44.dp),
            ) {
                Text("发送", color = Color.White)
            }
        }
    }
}

@Composable
private fun GradientBadge(text: String, brush: Brush) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun TextButtonLike(text: String) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF7ED))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color(0xFFF97316), fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
