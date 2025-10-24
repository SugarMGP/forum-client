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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.UpdateUserProfileRequest
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
                            AsyncImage(
                                model = avatar,
                                contentDescription = "头像",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        // TODO: Implement image picker
                                    },
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                            Text(
                                text = "点击更换头像",
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
            onNavigateBack()
        }
    }
}
