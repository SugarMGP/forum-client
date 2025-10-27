package org.jh.forum.client.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun ImagePicker(
    onImageSelected: (ByteArray, String) -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Create a handler that can be called
    val pickImage = {
        if (enabled) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val fileDialog = FileDialog(null as Frame?, "选择图片", FileDialog.LOAD)
                    fileDialog.setFilenameFilter { _, name ->
                        name.lowercase().endsWith(".jpg") ||
                                name.lowercase().endsWith(".jpeg") ||
                                name.lowercase().endsWith(".png") ||
                                name.lowercase().endsWith(".gif") ||
                                name.lowercase().endsWith(".webp")
                    }
                    fileDialog.isVisible = true

                    val directory = fileDialog.directory
                    val filename = fileDialog.file

                    if (directory != null && filename != null) {
                        try {
                            val file = File(directory, filename)
                            val bytes = file.readBytes()
                            onImageSelected(bytes, filename)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    // Provide the onClick via CompositionLocal
    androidx.compose.runtime.CompositionLocalProvider(
        LocalImagePickerClick provides pickImage
    ) {
        content()
    }
}

// CompositionLocal for providing the image picker click handler
actual val LocalImagePickerClick = staticCompositionLocalOf<() -> Unit> {
    { }
}
