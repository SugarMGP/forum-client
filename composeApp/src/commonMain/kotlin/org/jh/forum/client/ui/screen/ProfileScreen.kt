package org.jh.forum.client.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateToThemeSettings: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToPersonalPosts: () -> Unit = {}
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (isLoggedIn) {
            // 已登录状态
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(Dimensions.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
            ) {
                // 用户信息卡片
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimensions.spaceLarge),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 用户头像
                            Surface(
                                shape = CircleShape,
                                shadowElevation = Dimensions.elevationSmall,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                AsyncImage(
                                    model = userProfile?.avatar,
                                    contentDescription = "用户头像",
                                    modifier = Modifier
                                        .size(Dimensions.avatarExtraLarge)
                                        .padding(Dimensions.spaceExtraSmall)
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

                            // 用户名
                            Text(
                                text = userProfile?.nickname ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

                            // 用户签名（如果有）
                            userProfile?.signature?.let { signature ->
                                Text(
                                    text = signature,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                            
                            // 用户统计信息
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                UserStatItem(
                                    label = "获赞",
                                    value = userProfile?.likeCount?.toString() ?: "0"
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(Dimensions.spaceExtraLarge),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                )
                                UserStatItem(
                                    label = "帖子",
                                    value = userProfile?.postCount?.toString() ?: "0"
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(Dimensions.spaceExtraLarge),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                )
                                UserStatItem(
                                    label = "关注",
                                    value = userProfile?.followCount?.toString() ?: "0"
                                )
                            }
                        }
                    }
                }

                // 功能列表
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = AppIcons.Article,
                                title = "我的帖子",
                                onClick = onNavigateToPersonalPosts
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Dimensions.spaceMedium),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            ProfileMenuItem(
                                icon = AppIcons.Notifications,
                                title = "通知设置",
                                onClick = onNavigateToNotificationSettings
                            )
                        }
                    }
                }

                // 设置选项
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = AppIcons.Edit,
                                title = "编辑资料",
                                onClick = { /* 导航到编辑资料 */ }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Dimensions.spaceMedium),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            ProfileMenuItem(
                                icon = AppIcons.Lock,
                                title = "修改密码",
                                onClick = { /* 导航到修改密码 */ }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Dimensions.spaceMedium),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            ProfileMenuItem(
                                icon = AppIcons.Settings,
                                title = "主题设置",
                                onClick = onNavigateToThemeSettings
                            )
                        }
                    }
                }

                // 退出登录
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        ProfileMenuItem(
                            icon = AppIcons.Logout,
                            title = "退出登录",
                            onClick = { authViewModel.logout() },
                            textColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        } else {
            // 未登录状态
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    AppIcons.AccountCircle,
                    contentDescription = "未登录",
                    modifier = Modifier.size(Dimensions.avatarExtraLarge + Dimensions.spaceExtraLarge),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimensions.spaceLarge))

                Text(
                    text = "您还未登录",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .widthIn(min = 200.dp)
                        .height(Dimensions.buttonHeightLarge),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("登录", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Dimensions.spaceMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = textColor,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
            }
        }

        Spacer(modifier = Modifier.width(Dimensions.spaceMedium))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            AppIcons.KeyboardArrowRight,
            contentDescription = "箭头",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(Dimensions.iconMedium)
        )
    }
}

@Composable
private fun UserStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = Dimensions.spaceSmall)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(Dimensions.spaceExtraSmall))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}