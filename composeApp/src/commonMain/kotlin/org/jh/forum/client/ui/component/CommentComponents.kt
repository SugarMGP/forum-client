package org.jh.forum.client.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.CommentElement
import org.jh.forum.client.util.TimeUtils

@Composable
fun CommentItem(
    comment: CommentElement,
    onUpvote: () -> Unit,
    onPin: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onUserProfileClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 用户信息和更多选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像和用户昵称
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    comment.publisherInfo.id?.let {
                        onUserProfileClick(it)
                    }
                }
            ) {
                // 用户头像
                AsyncImage(
                    model = comment.publisherInfo.avatar,
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (comment.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "置顶",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = comment.publisherInfo.nickname ?: "未知用户",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (comment.isAuthor) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "楼主",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 更多操作菜单
            if (onPin != null || onDelete != null) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.More, "更多选项")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        onPin?.let {
                            DropdownMenuItem(
                                text = { Text(if (comment.isPinned) "取消置顶" else "置顶") },
                                onClick = {
                                    onPin()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PushPin, null)
                                }
                            )
                        }
                        onDelete?.let {
                            DropdownMenuItem(
                                text = { Text("删除") },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, null)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 评论内容
        Spacer(Modifier.height(12.dp))
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
        )

        // 图片展示（如果有）
        if (comment.pictures.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(comment.pictures) {
                    AsyncImage(
                        model = it.url,
                        contentDescription = "评论图片",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 时间和互动区
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 格式化时间
            Text(
                text = TimeUtils.formatTime(comment.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 点赞按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "点赞",
                    tint = if (comment.isLiked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onUpvote() }
                )
                if (comment.upvoteCount > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${comment.upvoteCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (comment.isLiked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CommentEditor(
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("写下你的评论...") },
            maxLines = 5,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (text.isNotBlank()) {
                    onSubmit(text)
                    text = ""
                }
            },
            enabled = text.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("发布")
        }
    }
}