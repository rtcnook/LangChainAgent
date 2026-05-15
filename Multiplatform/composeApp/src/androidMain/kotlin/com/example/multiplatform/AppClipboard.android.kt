package com.example.multiplatform.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

actual class AppClipboard {
    companion object {
        private var appContext: Context? = null
        fun init(context: Context) {
            appContext = context.applicationContext
        }
    }

    private val clipboardManager: ClipboardManager? by lazy {
        appContext?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

    actual fun copy(text: String) {
        val clip = ClipData.newPlainText("AI Chef", text)
        clipboardManager?.setPrimaryClip(clip)
    }

    actual fun readText(): String? {
        val clip = clipboardManager?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            return clip.getItemAt(0).text?.toString()
        }
        return null
    }
}
