package org.jh.forum.client.di

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

actual fun provideCookiesStorage(): CookiesStorage = JvmCookiesStorage()

class JvmCookiesStorage(
    private val file: Path = ensureCacheFile()
) : CookiesStorage {

    private val lock = ReentrantLock()
    private var memory: MutableList<SerializableCookie> = loadFromFile()

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        lock.withLock {
            memory.removeAll { it.name == cookie.name && it.domain == cookie.domain && it.path == cookie.path }
            memory.add(cookie.toSerializable())
            saveToFile()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun get(requestUrl: Url): List<Cookie> {
        val nowMillis = Clock.System.now().toEpochMilliseconds()
        return lock.withLock {
            // 过滤过期 cookie
            memory.removeIf { it.expiresMillis?.let { ex -> ex <= nowMillis } == true }
            saveToFile()
            // 返回与 url 匹配的 cookie（简化匹配：域名/路径）
            memory.filter { cookieMatchesUrl(it, requestUrl) }.map { it.toKtorCookie() }
        }
    }

    override fun close() {
        lock.withLock { saveToFile() }
    }

    private fun saveToFile() {
        try {
            val json = Json { prettyPrint = true }
            val s = json.encodeToString(memory)
            Files.writeString(file, s)
        } catch (e: Exception) {
            // 忽略写入错误（也可以 log）
            e.printStackTrace()
        }
    }

    private fun loadFromFile(): MutableList<SerializableCookie> {
        return try {
            if (Files.exists(file)) {
                val json = Files.readString(file)
                if (json.isBlank()) mutableListOf()
                else Json.decodeFromString<List<SerializableCookie>>(json).toMutableList()
            } else mutableListOf()
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    private fun cookieMatchesUrl(sc: SerializableCookie, url: Url): Boolean {
        val host = url.host
        val domainOk = sc.domain?.let { host.endsWith(it.trimStart('.')) } ?: true
        val pathOk = sc.path?.let { url.encodedPath.startsWith(it) } ?: true
        return domainOk && pathOk
    }

    companion object {
        private fun ensureCacheFile(): Path {
            val dir = Paths.get("cache")
            try {
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir)
                }
            } catch (_: Exception) { /* ignore */
            }
            val f = dir.resolve("cookies.json")
            if (!Files.exists(f)) {
                Files.writeString(f, "[]")
            }
            return f
        }
    }
}