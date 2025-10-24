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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToThemeSettings: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Dimensions.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
        ) {
            // Account Settings (only when logged in)
            if (isLoggedIn) {
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
                            SettingsMenuItem(
                                icon = AppIcons.Edit,
                                title = "编辑资料",
                                onClick = onNavigateToEditProfile
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Dimensions.spaceMedium),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            SettingsMenuItem(
                                icon = AppIcons.Lock,
                                title = "修改密码",
                                onClick = { /* Navigate to change password */ }
                            )
                        }
                    }
                }
            }

            // App Settings
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
                        SettingsMenuItem(
                            icon = AppIcons.Settings,
                            title = "主题设置",
                            onClick = onNavigateToThemeSettings
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Dimensions.spaceMedium),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        SettingsMenuItem(
                            icon = AppIcons.Notifications,
                            title = "通知设置",
                            onClick = onNavigateToNotificationSettings
                        )
                    }
                }
            }

            // Logout (only when logged in)
            if (isLoggedIn) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        SettingsMenuItem(
                            icon = AppIcons.Logout,
                            title = "退出登录",
                            onClick = { authViewModel.logout() },
                            textColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsMenuItem(
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
