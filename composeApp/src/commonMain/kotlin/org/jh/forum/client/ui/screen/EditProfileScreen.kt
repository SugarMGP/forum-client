package org.jh.forum.client.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.*
import org.jh.forum.client.data.model.UpdateUserProfileRequest
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.component.ImagePicker
import org.jh.forum.client.ui.component.LocalImagePickerClick
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var avatar by remember { mutableStateOf(userProfile?.avatar ?: "") }
    var nickname by remember { mutableStateOf(userProfile?.nickname ?: "") }
    var signature by remember { mutableStateOf(userProfile?.signature ?: "") }
    var gender by remember { mutableStateOf(userProfile?.gender ?: "UNKNOWN") }
    var profile by remember { mutableStateOf(userProfile?.profile ?: "") }
    var email by remember { mutableStateOf(userProfile?.email ?: "") }
    var collegeId by remember { mutableStateOf(userProfile?.collegeId ?: "") }
    var birthday by remember { mutableStateOf(userProfile?.birthday ?: "") }
    var birthdayVisible by remember { mutableStateOf(userProfile?.birthdayVisible ?: false) }
    var realnameVisible by remember { mutableStateOf(userProfile?.realnameVisible ?: false) }
    var studentIdVisible by remember { mutableStateOf(userProfile?.studentIdVisible ?: false) }

    var showSuccessMessage by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }

    val postViewModel = AppModule.postViewModel

    // Update state when profile loads
    LaunchedEffect(userProfile) {
        userProfile?.let { profileData ->
            avatar = profileData.avatar ?: ""
            nickname = profileData.nickname ?: ""
            signature = profileData.signature ?: ""
            gender = profileData.gender ?: "UNKNOWN"
            profile = profileData.profile ?: ""
            email = profileData.email ?: ""
            collegeId = profileData.collegeId ?: ""
            birthday = profileData.birthday ?: ""
            birthdayVisible = profileData.birthdayVisible ?: false
            realnameVisible = profileData.realnameVisible ?: false
            studentIdVisible = profileData.studentIdVisible ?: false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑资料") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Validate and save
                            if (nickname.isBlank()) {
                                return@TextButton
                            }

                            val request = UpdateUserProfileRequest(
                                avatar = avatar,
                                nickname = nickname,
                                signature = signature,
                                gender = gender,
                                profile = profile,
                                email = email,
                                collegeId = collegeId,
                                birthday = birthday.takeIf { it.isNotBlank() },
                                birthdayVisible = birthdayVisible,
                                realnameVisible = realnameVisible,
                                studentIdVisible = studentIdVisible
                            )

                            authViewModel.updateProfile(request)
                            showSuccessMessage = true
                        },
                        enabled = !isLoading && nickname.isNotBlank()
                    ) {
                        Text("保存")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimensions.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
            ) {
                // Avatar section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ImagePicker(
                                onImageSelected = { bytes, filename ->
                                    isUploadingImage = true
                                    postViewModel.uploadImage(bytes, filename) { url ->
                                        isUploadingImage = false
                                        if (url != null) {
                                            avatar = url
                                        }
                                    }
                                },
                                enabled = !isUploadingImage
                            ) {
                                val imagePickerClick = LocalImagePickerClick.current
                                Box(
                                    contentAlignment = Alignment.BottomEnd,
                                    modifier = Modifier.clickable(enabled = !isUploadingImage) {
                                        imagePickerClick.invoke()
                                    }
                                ) {
                                    AsyncImage(
                                        model = avatar,
                                        contentDescription = "头像",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Camera icon overlay
                                    Surface(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .offset(x = (-4).dp, y = (-4).dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isUploadingImage) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            } else {
                                                Icon(
                                                    AppIcons.PhotoCamera,
                                                    contentDescription = "更换头像",
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                            Text(
                                text = if (isUploadingImage) "上传中..." else "点击更换头像",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Basic Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium)
                        ) {
                            Text(
                                text = "基本信息",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                            )

                            OutlinedTextField(
                                value = nickname,
                                onValueChange = { nickname = it },
                                label = { Text("昵称") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = nickname.isBlank()
                            )

                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

                            OutlinedTextField(
                                value = signature,
                                onValueChange = { signature = it },
                                label = { Text("个性签名") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

                            // Gender selector
                            OutlinedCard(
                                onClick = { showGenderDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimensions.spaceMedium),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "性别",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                                    ) {
                                        Text(
                                            text = when (gender) {
                                                "MALE" -> "男"
                                                "FEMALE" -> "女"
                                                else -> "未知"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            AppIcons.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Extended Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium)
                        ) {
                            Text(
                                text = "个人简介",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                            )

                            OutlinedTextField(
                                value = profile,
                                onValueChange = { profile = it },
                                label = { Text("个人简介") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 5,
                                minLines = 3
                            )
                        }
                    }
                }

                // Contact Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium)
                        ) {
                            Text(
                                text = "联系方式",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("邮箱") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

                            OutlinedTextField(
                                value = collegeId,
                                onValueChange = { collegeId = it },
                                label = { Text("学院") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

                            // Birthday field with click to open date picker
                            OutlinedTextField(
                                value = birthday,
                                onValueChange = { },
                                label = { Text("生日") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                                enabled = false,
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                placeholder = { Text("选择生日日期") }
                            )
                        }
                    }
                }

                // Privacy Settings
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium)
                        ) {
                            Text(
                                text = "隐私设置",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "公开真实姓名",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Switch(
                                    checked = realnameVisible,
                                    onCheckedChange = { realnameVisible = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "公开学号",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Switch(
                                    checked = studentIdVisible,
                                    onCheckedChange = { studentIdVisible = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "公开生日",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Switch(
                                    checked = birthdayVisible,
                                    onCheckedChange = { birthdayVisible = it }
                                )
                            }
                        }
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Success message
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Dimensions.spaceMedium)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(Dimensions.spaceMedium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        Icon(
                            AppIcons.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "保存成功",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Error message
            errorMessage?.let { error ->
                LaunchedEffect(error) {
                    kotlinx.coroutines.delay(3000)
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(Dimensions.spaceMedium),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(Dimensions.spaceMedium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        Icon(
                            AppIcons.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Date picker dialog for birthday
        if (showDatePicker) {
            var selectedYear by remember { mutableStateOf(2000) }
            var selectedMonth by remember { mutableStateOf(1) }
            var selectedDay by remember { mutableStateOf(1) }

            // Parse current birthday if exists
            LaunchedEffect(birthday) {
                if (birthday.isNotBlank()) {
                    try {
                        val parts = birthday.split("-")
                        if (parts.size == 3) {
                            selectedYear = parts[0].toInt()
                            selectedMonth = parts[1].toInt()
                            selectedDay = parts[2].toInt()
                        }
                    } catch (e: Exception) {
                        // Use defaults if parsing fails
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showDatePicker = false },
                title = { Text("选择生日") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        // Year selector (1900 to current year)
                        // Using a fixed current year for simplicity and WASM compatibility
                        val currentYear = 2025
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { year ->
                                    if (year in 1900..currentYear) selectedYear = year
                                }
                            },
                            label = { Text("年") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Month selector (1-12)
                        OutlinedTextField(
                            value = selectedMonth.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { month ->
                                    if (month in 1..12) selectedMonth = month
                                }
                            },
                            label = { Text("月") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Day selector (1-31)
                        OutlinedTextField(
                            value = selectedDay.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { day ->
                                    if (day in 1..31) selectedDay = day
                                }
                            },
                            label = { Text("日") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Validate date is not today or in the future
                            val selectedDate = LocalDate(selectedYear, selectedMonth, selectedDay)
                            // Simple validation: just check if date is valid and not too far in future
                            val minDate = LocalDate(1900, 1, 2)
                            val maxDate = LocalDate(2025, 12, 31)

                            if (selectedDate >= minDate && selectedDate <= maxDate) {
                                birthday = buildString {
                                    append(selectedYear.toString().padStart(4, '0'))
                                    append('-')
                                    append(selectedMonth.toString().padStart(2, '0'))
                                    append('-')
                                    append(selectedDay.toString().padStart(2, '0'))
                                }
                                showDatePicker = false
                            }
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // Gender selection dialog
        if (showGenderDialog) {
            AlertDialog(
                onDismissRequest = { showGenderDialog = false },
                title = { Text("选择性别") },
                text = {
                    Column {
                        listOf(
                            "MALE" to "男",
                            "FEMALE" to "女",
                            "UNKNOWN" to "未知"
                        ).forEach { (value, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        gender = value
                                        showGenderDialog = false
                                    }
                                    .padding(Dimensions.spaceMedium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = gender == value,
                                    onClick = {
                                        gender = value
                                        showGenderDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                                Text(text = label)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGenderDialog = false }) {
                        Text("关闭")
                    }
                }
            )
        }
    }

    // Auto-hide success message
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            showSuccessMessage = false
        }
    }
}
