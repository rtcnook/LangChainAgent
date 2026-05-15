package com.example.multiplatform.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.multiplatform.data.HttpChatBackend
import com.example.multiplatform.domain.ChatController
import com.example.multiplatform.model.SelectedImage
import com.example.multiplatform.platform.AppClipboard
import com.example.multiplatform.platform.ImagePicker
import com.example.multiplatform.ui.components.ChatPanel
import com.example.multiplatform.ui.components.HeaderBar
import com.example.multiplatform.ui.components.InputBar
import com.example.multiplatform.ui.theme.WarmBackground
import kotlinx.coroutines.launch

@Composable
fun AiChefScreen() {
    var draft by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<SelectedImage?>(null) }
    val controller = remember { ChatController(HttpChatBackend()) }
    val imagePicker = remember { ImagePicker() }
    val clipboard = remember { AppClipboard() }
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
                onCopyMessage = { clipboard.copy(it) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            InputBar(
                value = draft,
                onValueChange = { draft = it },
                onSend = {
                    if (draft.isNotBlank() || selectedImage != null) {
                        val message = draft.ifBlank { "请识别这张图片" }
                        val image = selectedImage
                        draft = ""
                        selectedImage = null
                        scope.launch {
                            controller.sendMessage(message, image)
                        }
                    }
                },
                onPickImage = {
                    scope.launch {
                        imagePicker.pickImage()?.let { image ->
                            selectedImage = image
                        }
                    }
                },
                onClearImage = { selectedImage = null },
                selectedImageName = selectedImage?.fileName,
                enabled = !processing,
            )
        }
    }
}
