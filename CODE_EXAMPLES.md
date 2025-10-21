# Material 3 设计优化 - 代码示例

## 1. 主题定义对比

### 优化前
```kotlin
@Composable
fun ForumTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content
    )
}
```

### 优化后
```kotlin
// 自定义配色方案
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF01579B),
    // ... 完整的颜色定义
)

@Composable
fun ForumTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ForumTypography,
        shapes = ForumShapes,
        content = content
    )
}
```

## 2. 按钮样式对比

### 优化前
```kotlin
OutlinedButton(
    onClick = { onUpvoteClick(post.id) },
    modifier = Modifier.height(36.dp),
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(
        1.dp,
        if (post.isLiked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline
    ),
    colors = ButtonDefaults.outlinedButtonColors(
        contentColor = if (post.isLiked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        containerColor = if (post.isLiked) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else MaterialTheme.colorScheme.surface
    )
) {
    Icon(AppIcons.ThumbUp, "点赞", modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(4.dp))
    Text("${post.likeCount}", style = MaterialTheme.typography.bodyMedium)
}
```

### 优化后
```kotlin
FilledTonalButton(
    onClick = { onUpvoteClick(post.id) },
    modifier = Modifier.height(Dimensions.buttonHeightSmall),
    shape = MaterialTheme.shapes.small,
    colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = if (post.isLiked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (post.isLiked) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
) {
    Icon(AppIcons.ThumbUp, "点赞", modifier = Modifier.size(Dimensions.iconSmall))
    Spacer(modifier = Modifier.width(Dimensions.spaceExtraSmall))
    Text("${post.likeCount}", style = MaterialTheme.typography.labelLarge)
}
```

## 3. 卡片设计对比

### 优化前
```kotlin
Card(
    modifier = modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // 内容
    }
}
```

### 优化后
```kotlin
Card(
    modifier = modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    shape = MaterialTheme.shapes.medium
) {
    Column(modifier = Modifier.padding(Dimensions.spaceMedium)) {
        // 内容
    }
}
```

## 4. 文本字段对比

### 优化前
```kotlin
TextField(
    value = title,
    onValueChange = { title = it },
    label = { Text("标题") },
    modifier = Modifier.fillMaxWidth(),
    colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
    )
)
```

### 优化后
```kotlin
OutlinedTextField(
    value = title,
    onValueChange = { title = it },
    label = { Text("标题") },
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )
)
```

## 5. 间距使用对比

### 优化前
```kotlin
Column(modifier = Modifier.padding(16.dp)) {
    Spacer(modifier = Modifier.height(16.dp))
    // 内容
    Spacer(modifier = Modifier.height(8.dp))
}
```

### 优化后
```kotlin
Column(modifier = Modifier.padding(Dimensions.spaceMedium)) {
    Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
    // 内容
    Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
}
```

## 6. 颜色使用对比

### 优化前
```kotlin
// 硬编码颜色
background(Color.Black.copy(alpha = 0.5f))
Text("内容", color = Color.White)
Icon(icon, tint = Color.LightGray)
```

### 优化后
```kotlin
// 语义化颜色
background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
Text("内容", color = MaterialTheme.colorScheme.onPrimary)
Icon(icon, tint = MaterialTheme.colorScheme.onSurfaceVariant)
```

## 7. 错误消息显示对比

### 优化前
```kotlin
errorMessage?.let { message ->
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(16.dp)
    )
}
```

### 优化后
```kotlin
errorMessage?.let { message ->
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
```

## 8. 图标尺寸对比

### 优化前
```kotlin
Icon(AppIcons.ThumbUp, "点赞", modifier = Modifier.size(16.dp))
Icon(AppIcons.Person, "用户", modifier = Modifier.size(24.dp))
Icon(AppIcons.Settings, "设置", modifier = Modifier.size(20.dp))
```

### 优化后
```kotlin
Icon(AppIcons.ThumbUp, "点赞", modifier = Modifier.size(Dimensions.iconSmall))
Icon(AppIcons.Person, "用户", modifier = Modifier.size(Dimensions.iconMedium))
Icon(AppIcons.Settings, "设置", modifier = Modifier.size(Dimensions.iconMedium))
```

## 9. 头像显示对比

### 优化前
```kotlin
AsyncImage(
    model = user.avatar,
    contentDescription = "头像",
    modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
)
```

### 优化后
```kotlin
AsyncImage(
    model = user.avatar,
    contentDescription = "头像",
    modifier = Modifier
        .size(Dimensions.avatarMedium)
        .clip(CircleShape)
)
```

## 10. 主题选项卡对比

### 优化前
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = if (isSelected) 2.dp else 1.dp
    )
) {
    Row(modifier = Modifier.padding(16.dp)) {
        Icon(icon, null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
```

### 优化后
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = if (isSelected) 
            Dimensions.elevationMedium 
        else Dimensions.elevationSmall
    ),
    shape = MaterialTheme.shapes.medium
) {
    Row(modifier = Modifier.padding(Dimensions.spaceMedium)) {
        Icon(icon, null, modifier = Modifier.size(Dimensions.iconMedium))
        Spacer(modifier = Modifier.width(Dimensions.spaceMedium))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}
```

## 总结

### 关键改进点：

1. **统一的设计令牌** - 使用Dimensions对象统一管理所有尺寸
2. **语义化颜色** - 使用Material 3颜色系统而非硬编码
3. **标准化组件** - 所有类似组件使用相同的样式
4. **更好的类型安全** - 使用MaterialTheme.typography而非直接指定fontSize
5. **一致的形状** - 使用MaterialTheme.shapes统一圆角
6. **改进的可维护性** - 更改设计令牌即可全局更新

这些改进不仅提升了视觉一致性，还大大提高了代码的可维护性和可扩展性。
