package org.jh.forum.client.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jh.forum.client.ui.screen.LoginScreen
import org.jh.forum.client.ui.viewmodel.AuthViewModel

@Composable
fun AuthNavigation(
    authViewModel: AuthViewModel = remember { AuthViewModel() }
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // 登录成功后，状态会自动更新，导航会自动切换到主界面
                }
            )
        }
    }
}