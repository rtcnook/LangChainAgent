package com.example.multiplatform.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.multiplatform.platform.AppClipboard
import com.example.multiplatform.platform.ImagePicker
import com.example.multiplatform.ui.components.ChatPanel
import com.example.multiplatform.ui.components.HeaderBar
import com.example.multiplatform.ui.components.InputBar
import com.example.multiplatform.ui.theme.WarmBackground
import com.example.multiplatform.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun AiChefScreen() {
    val viewModel = remember { ChatViewModel() }
    val imagePicker = remember { ImagePicker() }
    val clipboard = remember { AppClipboard() }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.bind(this)
        viewModel.loadHistory()
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HeaderBar(
                    onNewChat = { scope.launch { viewModel.newChat() } },
                )
                ChatPanel(
                    messages = uiState.messages,
                    errorMessage = uiState.errorMessage,
                    onCopyMessage = { clipboard.copy(it) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
            InputBar(
                value = uiState.draft,
                onValueChange = viewModel::updateDraft,
                onSend = {
                    scope.launch { viewModel.sendMessage() }
                },
                onPickImage = {
                    scope.launch {
                        imagePicker.pickImage()?.let { image ->
                            viewModel.selectImage(image)
                        }
                    }
                },
                onPasteClipboard = viewModel::pasteIntoDraft,
                readClipboardText = {
                    clipboard.readText()?.takeIf { it.isNotBlank() }
                },
                onClearImage = viewModel::clearImage,
                selectedImageName = uiState.selectedImage?.fileName,
                enabled = !uiState.processing,
            )
        }
    }
}
