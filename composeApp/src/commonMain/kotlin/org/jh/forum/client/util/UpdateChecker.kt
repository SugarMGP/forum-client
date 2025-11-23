package org.jh.forum.client.util

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jh.forum.client.BuildKonfig

@Serializable
data class GithubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val name: String?,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("body") val body: String?
)

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val publishedAt: String,
    val releaseUrl: String
)

class UpdateChecker {
    companion object {
        private const val GITHUB_API_URL =
            "https://ghproxy.vip/https://api.github.com/repos/SugarMGP/forum-client/releases/latest"

        // Create a dedicated HttpClient with JSON support for GitHub API
        private val httpClient: HttpClient by lazy {
            HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(Logging) {
                    level = LogLevel.ALL
                }
            }
        }
    }

    suspend fun checkForUpdates(): UpdateInfo? {
        return try {
            val release: GithubRelease = httpClient.get(GITHUB_API_URL).body()

            val latestVersion = release.tagName.removePrefix("v")
            val hasUpdate = compareVersions(latestVersion, BuildKonfig.APP_VERSION) > 0

            println("Latest version: $latestVersion, Current version: ${BuildKonfig.APP_VERSION}, Has update: $hasUpdate, Published at: ${release.publishedAt}, URL: ${release.htmlUrl}")
            UpdateInfo(
                hasUpdate = hasUpdate,
                latestVersion = latestVersion,
                publishedAt = release.publishedAt,
                releaseUrl = release.htmlUrl
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(v1Parts.size, v2Parts.size)

        for (i in 0 until maxLength) {
            val v1 = v1Parts.getOrNull(i) ?: 0
            val v2 = v2Parts.getOrNull(i) ?: 0

            if (v1 > v2) return 1
            if (v1 < v2) return -1
        }

        return 0
    }
}
