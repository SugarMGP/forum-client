package org.jh.forum.client.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    currentDynamicColor: Boolean = false,
    currentSeedColor: Color = Color.Red
) {
    val authViewModel = AppModule.authViewModel
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var currentTheme by remember { mutableStateOf(ThemeMode.SYSTEM) }

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
                    icon = { Icon(it.icon, contentDescription = it.title) },
                    label = { Text(it.title) },
                    selected = currentDestination?.route?.startsWith(it.route) == true,
                    onClick = {
                        navController.navigate(it.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
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
                        refresh = refresh
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
                    "post_detail/{postId}",
                    enterTransition = { slideInTransition + fadeInTransition },
                    exitTransition = { fadeOutTransition },
                    popEnterTransition = { fadeInTransition },
                    popExitTransition = { slideOutPopTransition + fadeOutTransition }
                ) {
                    val postId = it.savedStateHandle.get<String>("postId")?.toLongOrNull() ?: 0L
                    PostDetailScreen(
                        postId = postId,
                        viewModel = AppModule.postViewModel,
                        commentViewModel = AppModule.commentViewModel,
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