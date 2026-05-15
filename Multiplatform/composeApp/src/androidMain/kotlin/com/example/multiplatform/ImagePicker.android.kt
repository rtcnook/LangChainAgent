package com.example.multiplatform.platform

import com.example.multiplatform.model.SelectedImage

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class ImagePicker(
    private val onLaunch: () -> Unit,
    private val setContinuation: (CancellableContinuation<SelectedImage?>) -> Unit
) {
    actual suspend fun pickImage(): SelectedImage? = suspendCancellableCoroutine { cont ->
        setContinuation(cont)
        onLaunch()
    }
}

@Composable
actual fun rememberImagePicker(): ImagePicker {
    val context = LocalContext.current
    var continuation: CancellableContinuation<SelectedImage?>? = null

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            continuation?.resume(null)
        } else {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                var name = "image.png"
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex) ?: "image.png"
                    }
                }
                cursor?.close()
                continuation?.resume(SelectedImage(name, bytes))
            } else {
                continuation?.resume(null)
            }
        }
        continuation = null
    }

    return remember {
        ImagePicker(
            onLaunch = { launcher.launch("image/*") },
            setContinuation = { continuation = it }
        )
    }
}
