package org.jh.forum.client.ui.component

import androidx.compose.runtime.Composable

// Platform-specific image picker implementation
@Composable
expect fun ImagePicker(
    onImageSelected: (ByteArray, String) -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
)

// CompositionLocal for providing the image picker click handler (for platforms that need it)
expect val LocalImagePickerClick: androidx.compose.runtime.ProvidableCompositionLocal<() -> Unit>
