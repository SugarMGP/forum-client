package org.jh.forum.client.di

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.jh.forum.client.data.api.ForumApi
import org.jh.forum.client.data.api.KtorForumApi
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.data.storage.DataStoreCookiesStorage
import org.jh.forum.client.data.storage.ThemePreferencesRepository
import org.jh.forum.client.data.storage.createDataStore
import org.jh.forum.client.ui.viewmodel.*

object AppModule {
    // 基础URL，可根据环境配置修改
    private const val BASE_URL = "https://bbs.mggovo.cn/"

    private val themeDataStore by lazy {
        createDataStore("theme.preferences_pb")
    }

    private val cookiesDataStore by lazy {
        createDataStore("cookies.preferences_pb")
    }

    val themePreferencesRepository by lazy {
        ThemePreferencesRepository(themeDataStore)
    }

    // 统一创建 HttpClient（common）
    private val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    encodeDefaults = true
                })
            }
            install(HttpCookies) {
                storage = DataStoreCookiesStorage(cookiesDataStore)
            }
            install(Logging) {
                level = LogLevel.ALL
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

    // ReplyViewModel 单例，用于回复功能
    val replyViewModel: ReplyViewModel by lazy {
        ReplyViewModel()
    }

    // MessageViewModel 单例，用于管理未读消息状态
    val messageViewModel: MessageViewModel by lazy {
        MessageViewModel()
    }
}