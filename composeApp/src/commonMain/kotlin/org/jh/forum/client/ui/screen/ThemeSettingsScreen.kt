package org.jh.forum.client.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import org.jh.forum.client.data.preferences.ThemePreferences
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.theme.supportsDynamicColor

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/**
 * Get display name for PaletteStyle (using English names)
 */
private val PaletteStyle.displayName: String
    get() = when (this) {
        PaletteStyle.TonalSpot -> "Tonal Spot"
        PaletteStyle.Neutral -> "Neutral"
        PaletteStyle.Vibrant -> "Vibrant"
        PaletteStyle.Expressive -> "Expressive"
        PaletteStyle.Rainbow -> "Rainbow"
        PaletteStyle.FruitSalad -> "Fruit Salad"
        PaletteStyle.Monochrome -> "Monochrome"
        PaletteStyle.Fidelity -> "Fidelity"
        PaletteStyle.Content -> "Content"
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
                                                text = style.displayName,
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
                enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)) +
                        expandVertically(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) +
                        shrinkVertically(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            val itemMinWidth = 88.dp
                            val columns = (maxWidth / itemMinWidth).toInt().coerceAtLeast(1)
                            val chunkedColors = ThemePreferences.availableColors.chunked(columns)

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chunkedColors.forEach { rowColors ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        rowColors.forEach { themeColor ->
                                            Box(
                                                modifier = Modifier.weight(1f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ColorSwatchPreview(
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
                                        }
                                        // Fill remaining space if row is incomplete
                                        val remaining = columns - rowColors.size
                                        if (remaining > 0) {
                                            repeat(remaining) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
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
                            "本应用支持 Material 3 动态取色（Android 12+），会根据您的壁纸自动调整主题色。您也可以选择不同的调色盘风格来改变配色方案的生成方式。关闭动态取色后，可以从 18 种预设颜色中选择主题色。"
                        } else {
                            "本应用使用 MaterialKolor 生成 Material 3 配色方案。您可以从 18 种预设颜色中选择主题色，并选择不同的调色盘风格来改变配色方案的生成方式。"
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
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.98f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
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
 * Color swatch preview component based on InstallerX-Revived design
 * Shows a squircle background with circular swatch inside
 * Displays primaryContainer (top half), tertiaryContainer (bottom-left), secondaryContainer (bottom-right)
 * with a central primary circle showing check if selected
 */
@Composable
fun ColorSwatchPreview(
    seedColor: Color,
    name: String,
    paletteStyle: PaletteStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Generate color scheme for preview (always use light mode for consistency)
    val scheme = remember(seedColor, paletteStyle) {
        dynamicColorScheme(
            seedColor = seedColor,
            isDark = false,
            style = paletteStyle
        )
    }

    val primaryForSwatch = scheme.primaryContainer.copy(alpha = 0.9f)
    val secondaryForSwatch = scheme.secondaryContainer.copy(alpha = 0.6f)
    val tertiaryForSwatch = scheme.tertiaryContainer.copy(alpha = 0.9f)
    val squircleBackgroundColor = scheme.primary.copy(alpha = 0.3f)

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "swatch_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .scale(scale)
    ) {
        // Squircle background with circular swatch inside
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(color = squircleBackgroundColor, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Inner circular swatch
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Primary container - top half (180 degrees)
                    drawArc(
                        color = primaryForSwatch,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true
                    )
                    // Tertiary container - bottom-left quarter
                    drawArc(
                        color = tertiaryForSwatch,
                        startAngle = 90f,
                        sweepAngle = 90f,
                        useCenter = true
                    )
                    // Secondary container - bottom-right quarter
                    drawArc(
                        color = secondaryForSwatch,
                        startAngle = 0f,
                        sweepAngle = 90f,
                        useCenter = true
                    )
                }

                // Central circle with primary color
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(scheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    // Show check icon if selected
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = scheme.inversePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Color name label
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}