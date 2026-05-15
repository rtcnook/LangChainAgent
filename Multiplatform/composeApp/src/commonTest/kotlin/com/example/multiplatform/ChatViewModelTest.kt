package com.example.multiplatform

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.multiplatform.model.SelectedImage
import com.example.multiplatform.ui.viewmodel.ChatViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatViewModelTest {

    @Test
    fun pastedTextUpdatesVisibleDraftState() {
        val viewModel = ChatViewModel(
            initialDraft = TextFieldValue(
                text = "前缀",
                selection = TextRange(2),
            ),
        )

        viewModel.pasteIntoDraft("复制内容")

        assertEquals("前缀复制内容", viewModel.uiState.value.draft.text)
        assertEquals(TextRange(6), viewModel.uiState.value.draft.selection)
    }

    @Test
    fun selectedImageLivesInUiState() {
        val viewModel = ChatViewModel()
        val image = SelectedImage("food.png", byteArrayOf(1))

        viewModel.selectImage(image)

        assertEquals(image, viewModel.uiState.value.selectedImage)
    }
}
