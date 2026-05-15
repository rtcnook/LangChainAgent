package com.example.multiplatform.platform

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual class AppClipboard {
    actual fun copy(text: String) {
        Toolkit.getDefaultToolkit()
            .systemClipboard
            .setContents(StringSelection(text), null)
    }
}
