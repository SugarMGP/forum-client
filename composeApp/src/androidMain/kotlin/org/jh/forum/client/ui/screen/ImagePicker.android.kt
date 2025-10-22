package org.jh.forum.client.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ImagePicker(
    onImageSelected: (ByteArray) -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        runCatching {
            // 读取文件内容到 ByteArray
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                onImageSelected(bytes)
            }
        }.onFailure { it.printStackTrace() }
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
