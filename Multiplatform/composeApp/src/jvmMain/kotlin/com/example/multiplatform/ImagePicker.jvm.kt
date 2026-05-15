package com.example.multiplatform.platform

import com.example.multiplatform.model.SelectedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter

actual class ImagePicker {
    actual suspend fun pickImage(): SelectedImage? = withContext(Dispatchers.IO) {
        val dialog = FileDialog(null as Frame?, "选择图片", FileDialog.LOAD).apply {
            filenameFilter = FilenameFilter { _, name ->
                val lower = name.lowercase()
                lower.endsWith(".jpg") ||
                    lower.endsWith(".jpeg") ||
                    lower.endsWith(".png") ||
                    lower.endsWith(".gif") ||
                    lower.endsWith(".webp")
            }
        }

        dialog.isVisible = true

        val directory = dialog.directory ?: return@withContext null
        val fileName = dialog.file ?: return@withContext null
        val file = java.io.File(directory, fileName)
        SelectedImage(
            fileName = file.name,
            bytes = file.readBytes(),
        )
    }
}

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePicker(): ImagePicker = remember { ImagePicker() }
