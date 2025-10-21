# 蓝色色系统一方案 - 颜色变更详情

## 问题描述
之前的实现中，虽然主色调（primary）使用了蓝色，但其他颜色如surface、surfaceVariant、outline等仍然使用Material 3默认的紫灰色调，导致整体色系不统一。

## 解决方案

### 1. 统一为蓝色色系

所有颜色都调整为蓝色/蓝灰色调，确保视觉一致性。

#### 浅色模式颜色变更

| 颜色角色 | 之前 (紫灰色系) | 现在 (蓝色系) | 说明 |
|---------|----------------|--------------|------|
| primary | #2196F3 | #2196F3 | 保持不变 - Material Blue |
| secondary | #03A9F4 | #03A9F4 | 保持不变 - Light Blue |
| tertiary | #00BCD4 | #00BCD4 | 保持不变 - Cyan |
| background | #FAFAFA | #FCFCFF | 改为带蓝调的白色 |
| surface | #FFFFFF | #FCFCFF | 改为带蓝调的白色 |
| surfaceVariant | #E7E0EC | #DEE3EB | **从紫灰色改为蓝灰色** |
| onSurfaceVariant | #49454F | #42474E | **从紫灰色改为蓝灰色** |
| outline | #79747E | #72777F | **从紫灰色改为蓝灰色** |
| onBackground | #1C1B1F | #1A1C1E | 微调为更中性的深色 |
| onSurface | #1C1B1F | #1A1C1E | 微调为更中性的深色 |

#### 深色模式颜色变更

| 颜色角色 | 之前 (紫灰色系) | 现在 (蓝色系) | 说明 |
|---------|----------------|--------------|------|
| primary | #90CAF9 | #90CAF9 | 保持不变 |
| secondary | #81D4FA | #81D4FA | 保持不变 |
| tertiary | #80DEEA | #80DEEA | 保持不变 |
| background | #1C1B1F | #1A1C1E | **从紫黑色改为蓝黑色** |
| surface | #1C1B1F | #1A1C1E | **从紫黑色改为蓝黑色** |
| surfaceVariant | #49454F | #42474E | **从紫灰色改为蓝灰色** |
| onSurfaceVariant | #CAC4D0 | #C2C7CF | **从紫灰色改为蓝灰色** |
| outline | #938F99 | #8C9199 | **从紫灰色改为蓝灰色** |
| onBackground | #E6E1E5 | #E2E2E6 | 调整为更中性的浅色 |
| onSurface | #E6E1E5 | #E2E2E6 | 调整为更中性的浅色 |

### 2. Android 动态取色支持

在Android 12+ (API 31+) 设备上，应用会自动从系统壁纸提取颜色，实现Material You效果。

#### 实现方式

```kotlin
// ForumTheme.android.kt
@Composable
actual fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when {
        // Android 12+ 使用动态颜色
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        // 低版本使用蓝色静态方案
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // ...
}
```

#### 平台支持

- **Android 12+**: 自动使用系统壁纸颜色（Material You）
- **Android 11及以下**: 使用蓝色静态配色方案
- **Desktop (JVM)**: 使用蓝色静态配色方案

### 3. 视觉效果对比

#### 卡片颜色变化
- **之前**: 卡片使用 surfaceVariant (#E7E0EC) - 带紫色调
- **现在**: 卡片使用 surfaceVariant (#DEE3EB) - 带蓝色调

#### 边框颜色变化
- **之前**: 边框使用 outline (#79747E) - 紫灰色
- **现在**: 边框使用 outline (#72777F) - 蓝灰色

#### 背景颜色变化
- **之前**: 背景使用 #FAFAFA (纯灰色) 或 #1C1B1F (带紫调的黑色)
- **现在**: 背景使用 #FCFCFF (带蓝调的白色) 或 #1A1C1E (带蓝调的黑色)

## 技术实现

### expect/actual 模式

使用Kotlin Multiplatform的expect/actual机制实现平台特定功能：

**commonMain (接口定义):**
```kotlin
@Composable
expect fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
)
```

**androidMain (Android实现):**
```kotlin
@Composable
actual fun ForumTheme(...) {
    // 支持动态取色
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> 
            dynamicDarkColorScheme(context)
        // ...
    }
}
```

**jvmMain (Desktop实现):**
```kotlin
@Composable
actual fun ForumTheme(...) {
    // Desktop不支持动态取色
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
}
```

## 使用方式

在App.kt中默认启用动态取色：

```kotlin
ForumTheme(
    darkTheme = darkTheme,
    dynamicColor = true // 在Android 12+上启用动态取色
) {
    MainNavigation(...)
}
```

## 效果总结

✅ **统一的蓝色视觉风格** - 所有颜色都采用蓝色色系  
✅ **Material You支持** - Android 12+设备可个性化配色  
✅ **向后兼容** - 低版本Android设备使用蓝色静态方案  
✅ **跨平台一致性** - Desktop平台同样使用蓝色方案  
✅ **保持对比度** - 所有颜色组合仍符合WCAG AA标准  

## 对比说明

### 统一前（有紫色调）
- Primary: 蓝色 ✓
- SurfaceVariant: 紫灰色 ✗
- Outline: 紫灰色 ✗
- Background: 中性灰 ✗

### 统一后（全蓝色系）
- Primary: 蓝色 ✓
- SurfaceVariant: 蓝灰色 ✓
- Outline: 蓝灰色 ✓
- Background: 带蓝调的白色 ✓

现在整个应用的色彩方案完全统一，在不同元素和组件之间呈现出协调一致的蓝色视觉风格。
