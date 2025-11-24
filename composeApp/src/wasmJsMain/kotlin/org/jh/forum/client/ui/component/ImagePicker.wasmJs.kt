package org.jh.forum.client.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun ImagePicker(
    onImageSelected: (ByteArray, String) -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val pickImage = {
        if (enabled) {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = ".jpg, .jpeg, .png, .gif, .webp"
            input.style.display = "none"

            input.onchange = {
                val file = input.files?.item(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = {
                        val buffer = reader.result as ArrayBuffer
                        val array = Int8Array(buffer)
                        val bytes = array.toByteArray()
                        onImageSelected(bytes, file.name)
                        null
                    }
                    reader.readAsArrayBuffer(file)
                }
                input.remove()
            }

            document.body?.appendChild(input)
            input.click()
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalImagePickerClick provides pickImage
    ) {
        content()
    }
}

actual val LocalImagePickerClick = staticCompositionLocalOf {
    { }
}