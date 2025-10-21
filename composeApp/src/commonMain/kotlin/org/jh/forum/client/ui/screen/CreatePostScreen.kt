package org.jh.forum.client.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import org.jh.forum.client.data.model.PostCategory
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)

// 自定义的流式布局组件
@Composable
fun WrapContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
    }
}

// 计算图片网格的行数
fun calculateRows(itemCount: Int): Int {
    return if (itemCount <= 3) 1 else if (itemCount <= 6) 2 else 3
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: PostViewModel,
    onBack: () -> Unit,
    onPostCreated: () -> Unit,
    onImagePickerClick: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PostCategory?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var topicInput by remember { mutableStateOf("") }
    var topics by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // 添加标签
    fun addTopic() {
        if (topicInput.isNotBlank() && !topics.contains(topicInput)) {
            topics = topics + topicInput
            topicInput = ""
        }
    }

    // 移除标签
    fun removeTopic(topic: String) {
        topics = topics.filter { it != topic }
    }

    // 提交帖子
    fun submitPost() {
        if (title.isBlank() || content.isBlank() || selectedCategory == null) {
            errorMessage = "请填写完整信息"
            return
        }

        isSubmitting = true
        viewModel.createPost(title, content, selectedCategory!!.value, topics, selectedImages) {
            isSubmitting = false
            if (it) {
                onPostCreated()
            } else {
                errorMessage = "发布失败，请重试"
            }
        }
    }

    // 上传图片
    fun uploadImage(bytes: ByteArray, filename: String) {
        isUploadingImage = true
        viewModel.uploadImage(bytes, filename) {
            isUploadingImage = false
            if (it != null) {
                selectedImages = selectedImages + it
            } else {
                errorMessage = "图片上传失败"
            }
        }
    }

    // 移除图片
    fun removeImage(imageUrl: String) {
        selectedImages = selectedImages.filter { it != imageUrl }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            // 一段时间后清除错误消息
            kotlinx.coroutines.delay(3000)
            errorMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布帖子") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimensions.spaceMedium)
        ) {
            // 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                placeholder = { Text("请输入帖子标题") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

            // 分类选择
            OutlinedTextField(
                value = selectedCategory?.displayName ?: "",
                onValueChange = {},
                label = { Text("分类") },
                placeholder = { Text("请选择帖子分类") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showCategoryMenu = !showCategoryMenu }) {
                        Icon(AppIcons.Category, contentDescription = "选择分类")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // 分类选择菜单
            DropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false }
            ) {
                PostCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            selectedCategory = category
                            showCategoryMenu = false
                        },
                        leadingIcon = {
                            val icon = when (category) {
                                PostCategory.CAMPUS -> AppIcons.School
                                PostCategory.EMOTION -> AppIcons.Favorite
                                PostCategory.STUDY -> AppIcons.MenuBook
                                PostCategory.CONTEST -> AppIcons.EmojiEvents
                                PostCategory.HOBBY -> AppIcons.SportsEsports
                                PostCategory.LOST -> AppIcons.Search
                                PostCategory.SECONDHAND -> AppIcons.Shop
                            }
                            Icon(
                                icon,
                                contentDescription = category.displayName,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

            // 内容输入
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("内容") },
                placeholder = { Text("请输入帖子内容") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = 10,
                minLines = 5,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

            // 标签输入
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = { Text("标签") },
                    placeholder = { Text("输入标签后按回车") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardActions = KeyboardActions(
                        onDone = { addTopic() }
                    ),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                Button(
                    onClick = { addTopic() },
                    modifier = Modifier.align(Alignment.CenterVertically).height(Dimensions.buttonHeightLarge),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("添加", style = MaterialTheme.typography.labelLarge)
                }
            }

            // 显示已添加的标签
            if (topics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                Box(modifier = Modifier.fillMaxWidth()) {
                    WrapContent {
                        topics.forEach { topic ->
                            FilterChip(
                                selected = false,
                                onClick = { removeTopic(topic) },
                                label = { Text(topic) },
                                modifier = Modifier.padding(end = Dimensions.spaceSmall, bottom = Dimensions.spaceSmall)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

            // 图片上传
            Button(
                onClick = onImagePickerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeightLarge),
                enabled = !isUploadingImage && selectedImages.size < 9,
                shape = MaterialTheme.shapes.small
            ) {
                if (isUploadingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.iconSmall),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                    Text("上传中...", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text("选择图片 (最多9张)", style = MaterialTheme.typography.labelLarge)
                }
            }

            // 图片预览
            if (selectedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(calculateRows(selectedImages.size)),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    items(selectedImages) {
                        val index = selectedImages.indexOf(it)
                        Box(
                            modifier = Modifier
                                .size(Dimensions.imagePreviewSmall)
                                .clip(MaterialTheme.shapes.medium)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = "图片 $index",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(Dimensions.spaceExtraSmall),
                                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
                                shape = CircleShape
                            ) {
                                IconButton(
                                    onClick = { removeImage(it) },
                                    modifier = Modifier.size(Dimensions.iconMedium)
                                ) {
                                    Icon(
                                        AppIcons.Close,
                                        contentDescription = "移除图片",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(Dimensions.iconSmall)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

            // 提交按钮（大屏幕上显示）
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = ::submitPost,
                    enabled = !isSubmitting && title.isNotBlank() && content.isNotBlank() && selectedCategory != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.buttonHeightLarge),
                    shape = MaterialTheme.shapes.small
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.iconSmall),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                    }
                    Text("发布帖子", style = MaterialTheme.typography.labelLarge)
                }
            }

            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Dimensions.spaceMedium)
                    )
                }
            }
        }
    }
}