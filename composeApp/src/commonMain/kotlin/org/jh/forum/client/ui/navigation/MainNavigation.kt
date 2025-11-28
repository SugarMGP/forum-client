package org.jh.forum.client.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.launch
import org.jh.forum.client.data.preferences.ThemePreferences
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.screen.*
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.util.UpdateChecker
import org.jh.forum.client.util.UpdateInfo
import org.jh.forum.client.util.openUrl

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "主页", AppIcons.Home)
    object Messages : BottomNavItem("messages", "消息", AppIcons.Message)
    object Profile : BottomNavItem("profile", "我的", AppIcons.Person)
}

// Default navigation transitions
private const val ANIMATION_DURATION = 300

private val fadeInTransition = fadeIn(animationSpec = tween(ANIMATION_DURATION))
private val fadeOutTransition = fadeOut(animationSpec = tween(ANIMATION_DURATION))
private val slideInTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(ANIMATION_DURATION)
)
private val slideOutTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth / 3 },
    animationSpec = tween(ANIMATION_DURATION)
)
private val slideInPopTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> -fullWidth / 3 },
    animationSpec = tween(ANIMATION_DURATION)
)
private val slideOutPopTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(ANIMATION_DURATION)
)

/**
 * Reusable Update Dialog component with smooth animations
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UpdateDialog(
    visible: Boolean,
    updateInfo: UpdateInfo?,
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit
) {
    AnimatedVisibility(
        visible = visible && updateInfo != null,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(200)),
        exit = scaleOut(
            animationSpec = tween(200),
            targetScale = 0.8f
        ) + fadeOut(animationSpec = tween(200))
    ) {
        // Custom dialog using Box overlay and Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(true) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = Dimensions.elevationMedium
                ),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(
                        "发现新版本",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Content
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                AppIcons.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "v${updateInfo?.latestVersion ?: ""}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        HorizontalDivider()
                        Text(
                            "发布时间: ${updateInfo?.publishedAt?.take(10) ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                "忽略本次",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                onDismiss()
                                updateInfo?.let { onDownload(it.releaseUrl) }
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                AppIcons.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("前往下载")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class
)
@Composable
fun MainNavigation(
    repository: ForumRepository = AppModule.forumRepository,
    onThemeChanged: (ThemeMode) -> Unit = { _ -> },
    onDynamicColorChanged: (Boolean) -> Unit = { _ -> },
    onSeedColorChanged: (Color) -> Unit = { _ -> },
    onPaletteStyleChanged: (PaletteStyle) -> Unit = { _ -> },
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    currentDynamicColor: Boolean = false,
    currentSeedColor: Color = ThemePreferences.defaultColor,
    currentPaletteStyle: PaletteStyle = PaletteStyle.TonalSpot
) {
    val authViewModel = AppModule.authViewModel
    val messageViewModel = AppModule.messageViewModel
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val hasUnreadMessages by messageViewModel.hasUnreadMessages.collectAsState()

    // Global state for update checking (used by both auto-check and manual check)
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    val scope = rememberCoroutineScope()
    val updateChecker = remember { UpdateChecker() }

    // 在组件初始化时检查用户登录状态和自动检查更新
    LaunchedEffect(Unit) {
        authViewModel.checkAuthStatus()

        // Automatically check for updates on app startup
        scope.launch {
            val info = updateChecker.checkForUpdates()
            if (info != null && info.hasUpdate) {
                updateInfo = info
                showUpdateDialog = true
            }
        }
    }

    // 监听登录状态变化，当退出登录时自动导航到登录页面
    LaunchedEffect(authViewModel.isLoggedIn.collectAsState().value, currentDestination) {
        // 确保currentDestination不为null，并且导航控制器已设置导航图
        if (!authViewModel.isLoggedIn.value && currentDestination != null && currentDestination.route != "login") {
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            listOf(
                BottomNavItem.Home,
                BottomNavItem.Messages,
                BottomNavItem.Profile
            ).forEach { it ->
                item(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (it.route == BottomNavItem.Messages.route && hasUnreadMessages) {
                                    Badge(
                                        modifier = Modifier.offset(x = 8.dp, y = (-4).dp)
                                    )
                                }
                            }
                        ) {
                            Icon(it.icon, contentDescription = it.title)
                        }
                    },
                    label = { Text(it.title) },
                    selected = currentDestination?.route?.startsWith(it.route) == true,
                    onClick = {
                        if (it.route == BottomNavItem.Messages.route) {
                            messageViewModel.cleanUnreadBadge()
                        }
                        navController.navigate(it.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    }
                )
            }
        },
        content = {
            NavHost(
                navController = navController,
                startDestination = "login",
            ) {
                composable(
                    BottomNavItem.Home.route,
                    enterTransition = { fadeInTransition },
                    exitTransition = { fadeOutTransition }
                ) {
                    PostListScreen(
                        onPostClick = { postId: Long ->
                            // 导航到帖子详情页
                            navController.navigate("post_detail/$postId")
                        },
                        onNavigateToCreatePost = {
                            // 导航到发帖页面
                            navController.navigate("create_post")
                        },
                        onUserClick = { userId: Long ->
                            // 导航到用户主页
                            navController.navigate("user_profile/$userId")
                        }
                    )
                }

                composable(
                    BottomNavItem.Messages.route,
                    enterTransition = { fadeInTransition },
                    exitTransition = { fadeOutTransition }
                ) {
                    MessagesScreen(
                        repository = repository,
                        onUserClick = { userId ->
                            navController.navigate("user_profile/$userId")
                        },
                        onNavigateToPost = { postId, highlightCommentId ->
                            val route = "post_detail/$postId?highlightCommentId=$highlightCommentId"
                            navController.navigate(route)
                        },
                        onNavigateToComment = { commentId, highlightReplyId ->
                            val route = "comment_replies/$commentId?highlightReplyId=$highlightReplyId"
                            navController.navigate(route)
                        }
                    )
                }

                composable(
                    BottomNavItem.Profile.route,
                    enterTransition = { fadeInTransition },
                    exitTransition = { fadeOutTransition }
                ) {
                    val currentUserId = authViewModel.userProfile.collectAsState().value?.userId
                    val isLoggedIn = authViewModel.isLoggedIn.collectAsState().value

                    if (isLoggedIn && currentUserId != null) {
                        UserProfileScreen(
                            userId = currentUserId,
                            authViewModel = authViewModel,
                            repository = repository,
                            onPostClick = { postId ->
                                navController.navigate("post_detail/$postId")
                            },
                            onNavigateBack = null, // No back button for bottom nav
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            },
                            onNavigateToPost = { postId, highlightCommentId ->
                                val route = "post_detail/$postId?highlightCommentId=$highlightCommentId"
                                navController.navigate(route)
                            },
                            onNavigateToComment = { commentId, highlightReplyId ->
                                val route = "comment_replies/$commentId?highlightReplyId=$highlightReplyId"
                                navController.navigate(route)
                            }
                        )
                    }
                }

                // Settings screens
                composable(
                    "settings",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { slideOutTransition + fadeOutTransition },
                    popEnterTransition = { slideInPopTransition + fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) {
                    SettingsScreen(
                        authViewModel = authViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToThemeSettings = {
                            navController.navigate("theme_settings")
                        },
                        onNavigateToNotificationSettings = {
                            navController.navigate("notification_settings")
                        },
                        onNavigateToEditProfile = {
                            navController.navigate("edit_profile")
                        },
                        onNavigateToAbout = {
                            navController.navigate("about")
                        },
                        onCheckForUpdates = {
                            scope.launch {
                                val info = updateChecker.checkForUpdates()
                                if (info != null && info.hasUpdate) {
                                    updateInfo = info
                                    showUpdateDialog = true
                                }
                            }
                        }
                    )
                }

                composable(
                    "edit_profile",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { slideOutTransition + fadeOutTransition },
                    popEnterTransition = { slideInPopTransition + fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) {
                    EditProfileScreen(
                        authViewModel = authViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    "theme_settings",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { slideOutTransition + fadeOutTransition },
                    popEnterTransition = { slideInPopTransition + fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) {
                    ThemeSettingsScreen(
                        currentTheme = currentThemeMode,
                        onThemeChanged = onThemeChanged,
                        useDynamicColor = currentDynamicColor,
                        onDynamicColorChanged = onDynamicColorChanged,
                        seedColor = currentSeedColor,
                        onSeedColorChanged = onSeedColorChanged,
                        paletteStyle = currentPaletteStyle,
                        onPaletteStyleChanged = onPaletteStyleChanged,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    "notification_settings",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { slideOutTransition + fadeOutTransition },
                    popEnterTransition = { slideInPopTransition + fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) {
                    NotificationSettingsScreen(
                        repository = repository,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    "about",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { slideOutTransition + fadeOutTransition },
                    popEnterTransition = { slideInPopTransition + fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) {
                    AboutScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onOpenGitHub = {
                            openUrl("https://github.com/SugarMGP/forum-client")
                        }
                    )
                }


                // 其他页面路由
                composable(
                    "login",
                    enterTransition = { fadeInTransition },
                    exitTransition = { fadeOutTransition }
                ) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                // 帖子详情页
                composable(
                    "post_detail/{postId}?highlightCommentId={highlightCommentId}",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { fadeOutTransition },
                    popEnterTransition = { fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) { backStackEntry ->
                    val postId = backStackEntry.savedStateHandle.get<String>("postId")?.toLongOrNull() ?: 0L
                    val highlightCommentId =
                        backStackEntry.savedStateHandle.get<String>("highlightCommentId")?.toLongOrNull() ?: 0L
                    PostDetailScreen(
                        postId = postId,
                        highlightCommentId = highlightCommentId,
                        viewModel = AppModule.postViewModel,
                        commentViewModel = AppModule.commentViewModel,
                        onBack = { navController.popBackStack() },
                        onUserClick = { userId ->
                            navController.navigate("user_profile/$userId")
                        },
                        onCommentClick = { commentId ->
                            navController.navigate("comment_replies/$commentId")
                        },
                        onPostUpdated = { postId, isLiked, likeCount ->
                            // Update the post in the list when like status changes
                            AppModule.postListViewModel.updatePostLikeStatus(postId, isLiked, likeCount)
                        },
                        onPostDeleted = { postId ->
                            // Remove the post from the list when it's deleted
                            AppModule.postListViewModel.removePost(postId)
                        }
                    )
                }

                // 评论回复页面
                composable(
                    "comment_replies/{commentId}?highlightReplyId={highlightReplyId}",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { fadeOutTransition },
                    popEnterTransition = { fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) { backStackEntry ->
                    val commentId = backStackEntry.savedStateHandle.get<String>("commentId")?.toLongOrNull() ?: 0L
                    val highlightReplyId =
                        backStackEntry.savedStateHandle.get<String>("highlightReplyId")?.toLongOrNull() ?: 0L
                    CommentRepliesScreen(
                        commentId = commentId,
                        highlightReplyId = highlightReplyId,
                        viewModel = AppModule.replyViewModel,
                        onBack = { navController.popBackStack() },
                        onUserClick = { userId ->
                            navController.navigate("user_profile/$userId")
                        }
                    )
                }


                // 发帖页面
                composable(
                    "create_post",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { fadeOutTransition },
                    popEnterTransition = { fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) {
                    CreatePostScreen(
                        viewModel = AppModule.postViewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onPostCreated = {
                            navController.popBackStack()
                        }
                    )
                }

                // 用户主页 - 查看其他用户
                composable(
                    "user_profile/{userId}",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { fadeOutTransition },
                    popEnterTransition = { fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) { backStackEntry ->
                    val userIdStr = backStackEntry.savedStateHandle.get<String>("userId") ?: "0"
                    val userId = userIdStr.toLongOrNull() ?: 0L
                    UserProfileScreen(
                        userId = userId,
                        authViewModel = authViewModel,
                        repository = repository,
                        onPostClick = { postId ->
                            navController.navigate("post_detail/$postId")
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToSettings = {
                            navController.navigate("settings")
                        },
                        onNavigateToPost = { postId, highlightCommentId ->
                            val route = "post_detail/$postId?highlightCommentId=$highlightCommentId"
                            navController.navigate(route)
                        },
                        onNavigateToComment = { commentId, highlightReplyId ->
                            val route = "comment_replies/$commentId?highlightReplyId=$highlightReplyId"
                            navController.navigate(route)
                        }
                    )
                }
            }
        }
    )

    // Global update dialog (shown for both auto-check and manual check)
    UpdateDialog(
        visible = showUpdateDialog,
        updateInfo = updateInfo,
        onDismiss = { showUpdateDialog = false },
        onDownload = { url -> openUrl(url) }
    )
}