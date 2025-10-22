package org.jh.forum.client.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

// 计算图片网格的行数
fun calculateRows(itemCount: Int): Int {
    return if (itemCount <= 3) 1 else if (itemCount <= 6) 2 else 3
}

// Platform-specific image picker implementation
@Composable
expect fun ImagePicker(
    onImageSelected: (ByteArray, String) -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
)

// CompositionLocal for providing the image picker click handler (for platforms that need it)
expect val LocalImagePickerClick: androidx.compose.runtime.ProvidableCompositionLocal<() -> Unit>

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: PostViewModel,
    onBack: () -> Unit,
    onPostCreated: () -> Unit
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
                title = { Text("发布帖子", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // Move submit button to the top bar for better UX
                    TextButton(
                        onClick = ::submitPost,
                        enabled = !isSubmitting && title.isNotBlank() && content.isNotBlank() && selectedCategory != null
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("发布", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Dimensions.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
        ) {
            // Title Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(Dimensions.spaceMedium)) {
                        Text(
                            text = "标题",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("请输入帖子标题", style = MaterialTheme.typography.bodyLarge) },
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
                    }
                }
            }

            // Category Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(Dimensions.spaceMedium)) {
                        Text(
                            text = "分类",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                        )
                        OutlinedTextField(
                            value = selectedCategory?.displayName ?: "",
                            onValueChange = {},
                            placeholder = { Text("请选择帖子分类", style = MaterialTheme.typography.bodyLarge) },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showCategoryMenu = !showCategoryMenu }) {
                                    Icon(
                                        if (showCategoryMenu) AppIcons.ExpandLess else AppIcons.ExpandMore,
                                        contentDescription = "选择分类"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Category selection dropdown
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            PostCategory.entries.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
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
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                                            Text(
                                                category.displayName,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Content Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(Dimensions.spaceMedium)) {
                        Text(
                            text = "内容",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                        )
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = { Text("分享你的想法...", style = MaterialTheme.typography.bodyLarge) },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp),
                            maxLines = 10,
                            minLines = 8,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // Topics Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(Dimensions.spaceMedium)) {
                        Text(
                            text = "话题标签",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = topicInput,
                                onValueChange = { topicInput = it },
                                placeholder = { Text("添加话题...", style = MaterialTheme.typography.bodyMedium) },
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
                            FilledIconButton(
                                onClick = { addTopic() },
                                enabled = topicInput.isNotBlank()
                            ) {
                                Icon(AppIcons.Add, contentDescription = "添加话题")
                            }
                        }

                        // Display added topics
                        if (topics.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                            ) {
                                topics.forEach { topic ->
                                    AssistChip(
                                        onClick = { removeTopic(topic) },
                                        label = { Text("#$topic", style = MaterialTheme.typography.bodyMedium) },
                                        trailingIcon = {
                                            Icon(
                                                AppIcons.Close,
                                                contentDescription = "删除",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Images Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(Dimensions.spaceMedium)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "图片 (${selectedImages.size}/9)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            ImagePicker(
                                onImageSelected = { bytes, filename -> uploadImage(bytes, filename) },
                                enabled = !isUploadingImage && selectedImages.size < 9
                            ) {
                                FilledTonalButton(
                                    onClick = LocalImagePickerClick.current,
                                    enabled = !isUploadingImage && selectedImages.size < 9
                                ) {
                                    Icon(
                                        AppIcons.Image,
                                        contentDescription = "选择图片",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                                    Text(
                                        if (isUploadingImage) "上传中..." else "选择图片",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        // Image preview grid
                        if (selectedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                            LazyHorizontalGrid(
                                rows = GridCells.Fixed(calculateRows(selectedImages.size)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (selectedImages.size <= 3) 100.dp else 200.dp),
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                            ) {
                                items(selectedImages) { imageUrl ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUrl),
                                            contentDescription = "图片预览",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp),
                                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                            shape = CircleShape
                                        ) {
                                            IconButton(
                                                onClick = { removeImage(imageUrl) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    AppIcons.Close,
                                                    contentDescription = "移除图片",
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Error message
            if (errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimensions.spaceMedium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                AppIcons.Error,
                                contentDescription = "错误",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                            Text(
                                errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// FlowRow implementation for topics
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}