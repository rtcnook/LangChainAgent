package com.example.multiplatform.platform

import com.example.multiplatform.model.SelectedImage

actual class ImagePicker {
    actual suspend fun pickImage(): SelectedImage? = null
}

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePicker(): ImagePicker = remember { ImagePicker() }
