package com.example.multiplatform.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multiplatform.ui.AppCopy
import kotlin.math.max
import kotlin.math.min

@Composable
fun InputBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onPickImage: () -> Unit,
    onPasteClipboard: (String) -> Unit,
    readClipboardText: () -> String?,
    onClearImage: () -> Unit,
    selectedImageName: String?,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.86f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButtonLike(text = "上传图片", enabled = enabled, onClick = onPickImage)
                BasicTextField(
                    value = value,
                    onValueChange = { if (enabled) onValueChange(it) },
                    singleLine = false,
                    minLines = 1,
                    textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF3F4F6).copy(alpha = 0.85f))
                        .onPreviewKeyEvent { event ->
                            if (
                                enabled &&
                                event.type == KeyEventType.KeyDown &&
                                event.key == Key.V &&
                                (event.isCtrlPressed || event.isMetaPressed)
                            ) {
                                val pastedText = readClipboardText()
                                if (!pastedText.isNullOrBlank()) {
                                    onPasteClipboard(pastedText)
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.text.isEmpty()) {
                                Text(
                                    text = AppCopy.inputPlaceholder,
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 15.sp,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Button(
                    onClick = onSend,
                    enabled = enabled && (value.text.isNotBlank() || selectedImageName != null),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316),
                        disabledContainerColor = Color(0xFFD1D5DB),
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                ) {
                    Text("发送", color = Color.White)
                }
            }
            if (selectedImageName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF7ED))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "已选择：$selectedImageName",
                        color = Color(0xFF9A3412),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "移除",
                        color = Color(0xFFEA580C),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(enabled = enabled, onClick = onClearImage),
                    )
                }
            }
        }
    }
}

internal fun TextFieldValue.insertText(textToInsert: String): TextFieldValue {
    val start = min(selection.start, selection.end).coerceIn(0, text.length)
    val end = max(selection.start, selection.end).coerceIn(0, text.length)
    val nextText = text.replaceRange(start, end, textToInsert)
    val nextCursor = start + textToInsert.length
    return copy(
        text = nextText,
        selection = TextRange(nextCursor),
    )
}
