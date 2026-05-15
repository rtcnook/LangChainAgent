package com.example.multiplatform.platform

import androidx.compose.runtime.Composable
import com.example.multiplatform.model.SelectedImage

expect class ImagePicker {
    suspend fun pickImage(): SelectedImage?
}

@Composable
expect fun rememberImagePicker(): ImagePicker
