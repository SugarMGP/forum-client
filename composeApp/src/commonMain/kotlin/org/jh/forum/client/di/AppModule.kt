package org.jh.forum.client.di

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.jh.forum.client.data.api.ForumApi
import org.jh.forum.client.data.api.KtorForumApi
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.ui.viewmodel.AuthViewModel
import org.jh.forum.client.ui.viewmodel.CommentViewModel
import org.jh.forum.client.ui.viewmodel.PostListViewModel
import org.jh.forum.client.ui.viewmodel.PostViewModel

object AppModule {
    // 基础URL，可根据环境配置修改
    private const val BASE_URL = "http://115.190.140.99:8080"

    // 统一创建 HttpClient（common）
    private val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    encodeDefaults = true
                })
            }
            install(HttpCookies) {
                storage = provideCookiesStorage()
            }
            install(Logging) {
                level = LogLevel.ALL
            }

            // 添加header  X-JH-Operator 为 1
            defaultRequest {
                header("X-JH-Operator", "1")
            }
        }
    }

    // 使用 KtorForumApi 实现
    val forumApi: ForumApi by lazy {
        KtorForumApi(httpClient, BASE_URL)
    }

    val forumRepository: ForumRepository by lazy {
        ForumRepository(forumApi)
    }

    // AuthViewModel 单例，确保整个应用共享同一个实例
    val authViewModel: AuthViewModel by lazy {
        AuthViewModel()
    }

    // PostListViewModel 单例，确保整个应用共享同一个实例
    val postListViewModel: PostListViewModel by lazy {
        PostListViewModel()
    }

    // PostViewModel 单例，用于帖子详情页
    val postViewModel: PostViewModel by lazy {
        PostViewModel()
    }

    // CommentViewModel 单例，用于评论功能
    val commentViewModel: CommentViewModel by lazy {
        CommentViewModel()
    }
}