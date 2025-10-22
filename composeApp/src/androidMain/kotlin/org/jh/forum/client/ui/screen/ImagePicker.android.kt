package org.jh.forum.client.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.io.InputStream

@Composable
actual fun ImagePicker(
    onImageSelected: (InputStream) -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val input: InputStream? = context.contentResolver.openInputStream(it)
                if (input != null) {
                    // Pass the stream to caller; lifecycle/closing is managed downstream
                    onImageSelected(input)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalImagePickerClick provides {
            if (enabled) launcher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    ) {
        Box(
            modifier = Modifier.clickable(enabled = enabled) {
                launcher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        ) {
            content()
        }
    }
}

actual val LocalImagePickerClick = staticCompositionLocalOf<() -> Unit> {
    { }
}
