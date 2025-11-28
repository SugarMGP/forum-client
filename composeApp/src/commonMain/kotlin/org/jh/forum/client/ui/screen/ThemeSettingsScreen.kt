package org.jh.forum.client.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import org.jh.forum.client.data.preferences.ThemePreferences
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.theme.supportsDynamicColor

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/**
 * Get localized name for PaletteStyle
 */
private fun PaletteStyle.getLocalizedName(): String = when (this) {
    PaletteStyle.TonalSpot -> "色调斑点"
    PaletteStyle.Neutral -> "中性"
    PaletteStyle.Vibrant -> "鲜艳"
    PaletteStyle.Expressive -> "表达性"
    PaletteStyle.Rainbow -> "彩虹"
    PaletteStyle.FruitSalad -> "水果沙拉"
    PaletteStyle.Monochrome -> "单色"
    PaletteStyle.Fidelity -> "保真"
    PaletteStyle.Content -> "内容"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    currentTheme: ThemeMode = ThemeMode.SYSTEM,
    onThemeChanged: (ThemeMode) -> Unit = {},
    useDynamicColor: Boolean = false,
    onDynamicColorChanged: (Boolean) -> Unit = {},
    seedColor: Color = ThemePreferences.defaultColor,
    onSeedColorChanged: (Color) -> Unit = {},
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    onPaletteStyleChanged: (PaletteStyle) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var selectedTheme by remember(currentTheme) { mutableStateOf(currentTheme) }
    var dynamicColorEnabled by remember(useDynamicColor) { mutableStateOf(useDynamicColor) }
    var selectedSeedColor by remember(seedColor) { mutableStateOf(seedColor) }
    var selectedPaletteStyle by remember(paletteStyle) { mutableStateOf(paletteStyle) }
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
            // Theme mode selection card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.spaceMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
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
                }
            }

            // Dynamic color toggle (only show if supported)
            if (isDynamicColorSupported) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(Dimensions.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
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
                    }
                }
            }

            // Palette style selector card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.spaceMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    Text(
                        text = "调色盘风格",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "选择配色方案的生成风格",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Palette style chips in a flow layout
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        PaletteStyle.entries.chunked(3).forEach { rowStyles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                            ) {
                                rowStyles.forEach { style ->
                                    FilterChip(
                                        modifier = Modifier.weight(1f),
                                        selected = selectedPaletteStyle == style,
                                        onClick = {
                                            selectedPaletteStyle = style
                                            onPaletteStyleChanged(style)
                                        },
                                        label = {
                                            Text(
                                                text = style.getLocalizedName(),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        leadingIcon = if (selectedPaletteStyle == style) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null
                                    )
                                }
                                // Fill remaining space if row is incomplete
                                repeat(3 - rowStyles.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Color picker card (only show when dynamic color is disabled or not supported)
            AnimatedVisibility(
                visible = !dynamicColorEnabled || !isDynamicColorSupported,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(Dimensions.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        Text(
                            text = "主题颜色",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "选择种子颜色以生成主题配色",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Color grid using BoxWithConstraints for responsive layout
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val columns = when {
                                maxWidth < 300.dp -> 4
                                maxWidth < 400.dp -> 5
                                else -> 6
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                            ) {
                                ThemePreferences.availableColors.chunked(columns).forEach { rowColors ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                                    ) {
                                        rowColors.forEach { themeColor ->
                                            ColorSwatchPreview(
                                                modifier = Modifier.weight(1f),
                                                seedColor = themeColor.color,
                                                name = themeColor.name,
                                                paletteStyle = selectedPaletteStyle,
                                                isSelected = selectedSeedColor == themeColor.color,
                                                onClick = {
                                                    selectedSeedColor = themeColor.color
                                                    onSeedColorChanged(themeColor.color)
                                                }
                                            )
                                        }
                                        // Fill remaining space if row is incomplete
                                        repeat(columns - rowColors.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                            "本应用支持 Material 3 动态取色（Android 12+），会根据您的壁纸自动调整主题色。您也可以选择不同的调色盘风格来改变配色方案的生成方式。关闭动态取色后，可以从 17 种预设颜色中选择主题色。"
                        } else {
                            "本应用使用 MaterialKolor 生成 Material 3 配色方案。您可以从 17 种预设颜色中选择主题色，并选择不同的调色盘风格来改变配色方案的生成方式。"
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

/**
 * Color swatch preview component that shows a four-quarter pie chart
 * displaying primaryContainer, secondaryContainer, and tertiaryContainer colors
 */
@Composable
fun ColorSwatchPreview(
    modifier: Modifier = Modifier,
    seedColor: Color,
    name: String,
    paletteStyle: PaletteStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Generate color scheme for preview
    val colorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = false,
        style = paletteStyle
    )

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(Dimensions.spaceExtraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            // Draw the color swatch as a circular pie chart
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            ) {
                val sweepAngle = 90f

                // Primary container - top-left quarter
                drawArc(
                    color = colorScheme.primaryContainer,
                    startAngle = 180f,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                // Secondary container - top-right quarter
                drawArc(
                    color = colorScheme.secondaryContainer,
                    startAngle = 270f,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                // Tertiary container - bottom-right quarter
                drawArc(
                    color = colorScheme.tertiaryContainer,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                // Surface variant - bottom-left quarter
                drawArc(
                    color = colorScheme.surfaceVariant,
                    startAngle = 90f,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                // Draw selection border if selected
                if (isSelected) {
                    drawCircle(
                        color = colorScheme.primary,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }

            // Check icon when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1
        )
    }
}