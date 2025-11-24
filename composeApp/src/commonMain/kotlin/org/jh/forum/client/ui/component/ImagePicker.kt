package org.jh.forum.client.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal

// Platform-specific image picker implementation
@Composable
expect fun ImagePicker(
    onImageSelected: (ByteArray, String) -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
)

// CompositionLocal for providing the image picker click handler (for platforms that need it)
expect val LocalImagePickerClick: ProvidableCompositionLocal<() -> Unit>
