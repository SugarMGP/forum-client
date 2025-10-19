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
                title = { Text("我的") }
            )
        }
    ) { paddingValues ->
        if (isLoggedIn) {
            // 已登录状态
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 用户信息卡片
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 用户头像
                            AsyncImage(
                                model = userProfile?.avatar,
                                contentDescription = "用户头像",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 用户名
                            Text(
                                text = userProfile?.nickname ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 用户签名（如果有）
                            userProfile?.signature?.let { signature ->
                                Text(
                                    text = signature,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 功能列表
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = AppIcons.Article,
                                title = "我的帖子",
                                onClick = onNavigateToPersonalPosts
                            )
                            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = AppIcons.Edit,
                                title = "编辑资料",
                                onClick = { /* 导航到编辑资料 */ }
                            )
                            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                            ProfileMenuItem(
                                icon = AppIcons.Lock,
                                title = "修改密码",
                                onClick = { /* 导航到修改密码 */ }
                            )
                            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ProfileMenuItem(
                            icon = AppIcons.Logout,
                            title = "退出登录",
                            onClick = { authViewModel.logout() },
                            textColor = MaterialTheme.colorScheme.error
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
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "您还未登录",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.widthIn(min = 200.dp)
                ) {
                    Text("登录")
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            AppIcons.KeyboardArrowRight,
            contentDescription = "箭头",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}