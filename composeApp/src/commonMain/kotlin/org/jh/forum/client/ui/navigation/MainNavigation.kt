package org.jh.forum.client.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.screen.*
import org.jh.forum.client.ui.theme.AppIcons

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
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    currentDynamicColor: Boolean = false,
    currentSeedColor: Color = Color.Red
) {
    val authViewModel = AppModule.authViewModel
    val messageViewModel = AppModule.messageViewModel
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var currentTheme by remember(currentThemeMode) { mutableStateOf(currentThemeMode) }
    var homeRefreshTrigger by remember { mutableStateOf(0) }
    val hasUnreadMessages by messageViewModel.hasUnreadMessages.collectAsState()

    // 在组件初始化时检查用户登录状态
    LaunchedEffect(Unit) {
        authViewModel.checkAuthStatus()
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
                        // If clicking home button while already on home, trigger refresh
                        if (it.route == BottomNavItem.Home.route &&
                            currentDestination?.route?.startsWith(BottomNavItem.Home.route) == true
                        ) {
                            homeRefreshTrigger++
                        }
                        if (it.route == BottomNavItem.Messages.route) {
                            messageViewModel.cleanUnreadBadge()
                        }
                        navController.navigate(it.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }
        },
        content = {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
            ) {
                composable(
                    BottomNavItem.Home.route + "?refresh={refresh}",
                    enterTransition = { fadeInTransition },
                    exitTransition = { fadeOutTransition }
                ) { backStackEntry ->
                    val refresh =
                        backStackEntry.savedStateHandle.get<String>("refresh")?.toBoolean()
                            ?: false
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
                        },
                        refresh = refresh || homeRefreshTrigger > 0,
                        onRefreshComplete = {
                            if (homeRefreshTrigger > 0) {
                                homeRefreshTrigger = 0
                            }
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
                        currentTheme = currentTheme,
                        onThemeChanged = { themeMode ->
                            currentTheme = themeMode
                            onThemeChanged(themeMode)
                        },
                        useDynamicColor = currentDynamicColor,
                        onDynamicColorChanged = onDynamicColorChanged,
                        seedColor = currentSeedColor,
                        onSeedColorChanged = onSeedColorChanged,
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
                        }
                    )
                }
            }
        }
    )
}