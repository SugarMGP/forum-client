package org.jh.forum.client.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jh.forum.client.data.preferences.ThemePreferences
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.theme.supportsDynamicColor

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    currentTheme: ThemeMode = ThemeMode.SYSTEM,
    onThemeChanged: (ThemeMode) -> Unit = {},
    useDynamicColor: Boolean = false,
    onDynamicColorChanged: (Boolean) -> Unit = {},
    seedColor: Color = ThemePreferences.Red,
    onSeedColorChanged: (Color) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    var dynamicColorEnabled by remember { mutableStateOf(useDynamicColor) }
    var selectedSeedColor by remember { mutableStateOf(seedColor) }
    val isDynamicColorSupported = supportsDynamicColor()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "返回"
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
        ) {
            // Theme mode selection
            Text(
                text = "主题模式",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            ThemeMode.entries.forEach { themeMode ->
                ThemeOption(
                    title = when (themeMode) {
                        ThemeMode.SYSTEM -> "跟随系统"
                        ThemeMode.LIGHT -> "浅色模式"
                        ThemeMode.DARK -> "深色模式"
                    },
                    description = when (themeMode) {
                        ThemeMode.SYSTEM -> "应用将根据系统设置自动切换主题"
                        ThemeMode.LIGHT -> "始终使用浅色主题"
                        ThemeMode.DARK -> "始终使用深色主题"
                    },
                    icon = when (themeMode) {
                        ThemeMode.SYSTEM -> AppIcons.SettingsBrightness
                        ThemeMode.LIGHT -> AppIcons.LightMode
                        ThemeMode.DARK -> AppIcons.DarkMode
                    },
                    isSelected = selectedTheme == themeMode,
                    onClick = {
                        selectedTheme = themeMode
                        onThemeChanged(themeMode)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Dimensions.spaceSmall))

            // Dynamic color toggle (only show if supported)
            if (isDynamicColorSupported) {
                Text(
                    text = "动态取色",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dynamicColorEnabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.spaceMedium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "启用动态颜色",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (dynamicColorEnabled) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.height(Dimensions.spaceExtraSmall))
                            Text(
                                text = "根据您的壁纸自动调整主题色",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (dynamicColorEnabled) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Switch(
                            checked = dynamicColorEnabled,
                            onCheckedChange = { enabled ->
                                dynamicColorEnabled = enabled
                                onDynamicColorChanged(enabled)
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Dimensions.spaceSmall))
            }

            // Color picker (only show when dynamic color is disabled or not supported)
            if (!dynamicColorEnabled || !isDynamicColorSupported) {
                Text(
                    text = "主题颜色",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    ThemePreferences.availableColors.forEach { themeColor ->
                        ColorOption(
                            modifier = Modifier.weight(1f),
                            name = themeColor.name,
                            color = themeColor.color,
                            isSelected = selectedSeedColor == themeColor.color,
                            onClick = {
                                selectedSeedColor = themeColor.color
                                onSeedColorChanged(themeColor.color)
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Dimensions.spaceSmall))
            }

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.spaceMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    Text(
                        text = "关于主题定制",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = if (isDynamicColorSupported) {
                            "本应用支持动态取色（Android 12+），会根据您的壁纸自动调整主题色。关闭动态取色后，您可以从预设颜色中选择主题色。"
                        } else {
                            "本应用使用 MaterialKolor 生成主题配色方案。您可以从预设的颜色中选择主题色，应用将自动生成配套的色彩体系。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) Dimensions.elevationMedium else Dimensions.elevationSmall
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(Dimensions.iconMedium)
            )

            Spacer(modifier = Modifier.width(Dimensions.spaceMedium))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(modifier = Modifier.height(Dimensions.spaceExtraSmall))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun ColorOption(
    modifier: Modifier = Modifier,
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(Dimensions.spaceSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
        }
    }
}