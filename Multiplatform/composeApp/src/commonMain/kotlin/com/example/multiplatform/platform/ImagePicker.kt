package com.example.multiplatform.platform

import com.example.multiplatform.model.SelectedImage

expect class ImagePicker() {
    suspend fun pickImage(): SelectedImage?
}
