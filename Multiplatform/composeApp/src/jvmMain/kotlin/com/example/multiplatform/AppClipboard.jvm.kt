package com.example.multiplatform.platform

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

actual class AppClipboard {
    actual fun copy(text: String) {
        Toolkit.getDefaultToolkit()
            .systemClipboard
            .setContents(StringSelection(text), null)
    }

    actual fun readText(): String? {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        return if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            clipboard.getData(DataFlavor.stringFlavor) as? String
        } else {
            null
        }
    }
}
