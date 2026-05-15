package com.example.multiplatform.platform

import com.example.multiplatform.model.SelectedImage

actual class ImagePicker {
    actual suspend fun pickImage(): SelectedImage? = null
}
