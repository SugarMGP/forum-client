package org.jh.forum.client.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
    
    Box(
        modifier = Modifier.clickable(enabled = enabled) {
            scope.launch {
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
                    withContext(Dispatchers.IO) {
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
    ) {
        content()
    }
}
