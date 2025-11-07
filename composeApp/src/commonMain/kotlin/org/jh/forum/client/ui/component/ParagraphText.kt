package org.jh.forum.client.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ParagraphText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    paragraphSpacing: Dp = 8.dp
) {
    val paragraphs = text.split("\n").filter { text -> text.isNotBlank() }

    Column(modifier = modifier) {
        paragraphs.forEachIndexed { index, paragraph ->
            Text(
                text = paragraph,
                style = style
            )

            // 在手动换行符处分段添加额外间距
            if (index != paragraphs.lastIndex) {
                Spacer(modifier = Modifier.height(paragraphSpacing))
            }
        }
    }
}
