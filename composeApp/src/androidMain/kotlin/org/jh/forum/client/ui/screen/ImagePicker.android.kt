package org.jh.forum.client.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
actual fun ImagePicker(
    onImageSelected: (ByteArray, String) -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                inputStream?.use { stream ->
                    val outputStream = ByteArrayOutputStream()
                    stream.copyTo(outputStream)
                    val bytes = outputStream.toByteArray()
                    
                    // Get filename from URI or generate one
                    val filename = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        cursor.getString(nameIndex)
                    } ?: "image_${System.currentTimeMillis()}.jpg"
                    
                    onImageSelected(bytes, filename)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Box(
        modifier = Modifier.clickable(enabled = enabled) { 
            launcher.launch("image/*") 
        }
    ) {
        content()
    }
}
