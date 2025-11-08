package org.jh.forum.client.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Common KMP implementation of CookiesStorage using DataStore Preferences.
 *
 * This implementation stores HTTP cookies persistently using DataStore,
 * allowing the Ktor HTTP client to maintain authentication and session state
 * across app restarts.
 *
 * Cookies are stored as key-value pairs in the format:
 * "name|domain|path" -> "value|expires|secure|httpOnly|maxAge"
 */
class DataStoreCookiesStorage(
    private val dataStore: DataStore<Preferences>
) : CookiesStorage {

    private val mutex = Mutex()

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        mutex.withLock {
            val key = cookieKey(cookie.name, cookie.domain, cookie.path)
            val value = cookieValue(cookie)
            
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun get(requestUrl: Url): List<Cookie> {
        return mutex.withLock {
            val nowMillis = Clock.System.now().toEpochMilliseconds()
            val allCookies = mutableMapOf<String, String>()
            
            dataStore.data.map { preferences ->
                preferences.asMap().mapNotNull { (key, value) ->
                    @Suppress("USELESS_IS_CHECK")
                    if (key is Preferences.Key<*> && value is String) {
                        key.name to value
                    } else null
                }.toMap()
            }.first().also { allCookies.putAll(it) }
            
            // Parse cookies and filter expired ones
            val validCookies = mutableListOf<Cookie>()
            val expiredKeys = mutableListOf<String>()
            
            allCookies.forEach { (key, value) ->
                try {
                    val cookie = parseCookie(key, value)
                    
                    // Check if expired
                    val expires = cookie.expires?.timestamp
                    if (expires != null && expires <= nowMillis) {
                        expiredKeys.add(key)
                    } else {
                        // Check if matches URL
                        if (cookieMatchesUrl(cookie, requestUrl)) {
                            validCookies.add(cookie)
                        }
                    }
                } catch (_: Exception) {
                    // Invalid cookie, mark for deletion
                    expiredKeys.add(key)
                }
            }
            
            // Remove expired cookies
            if (expiredKeys.isNotEmpty()) {
                dataStore.edit { preferences ->
                    expiredKeys.forEach { key ->
                        preferences.remove(stringPreferencesKey(key))
                    }
                }
            }
            
            validCookies
        }
    }

    override fun close() {
        // DataStore handles cleanup automatically
    }

    /**
     * Generate a unique key for the cookie based on name, domain, and path
     */
    private fun cookieKey(name: String, domain: String?, path: String?): String {
        return "${name}|${domain ?: ""}|${path ?: ""}"
    }

    /**
     * Encode cookie properties as a pipe-delimited string
     */
    private fun cookieValue(cookie: Cookie): String {
        val parts = mutableListOf<String>()
        parts.add(cookie.value)
        parts.add(cookie.expires?.timestamp?.toString() ?: "")
        parts.add(if (cookie.secure) "1" else "0")
        parts.add(if (cookie.httpOnly) "1" else "0")
        parts.add(cookie.maxAge?.toString() ?: "")
        parts.add(cookie.encoding.name)
        // Encode extensions as key1=val1,key2=val2
        parts.add(cookie.extensions.entries.joinToString(",") { "${it.key}=${it.value ?: ""}" })
        return parts.joinToString("|")
    }

    /**
     * Parse a cookie from key and value strings
     */
    private fun parseCookie(key: String, value: String): Cookie {
        val keyParts = key.split("|")
        require(keyParts.size == 3) { "Invalid cookie key format" }
        
        val name = keyParts[0]
        val domain = keyParts[1].ifEmpty { null }
        val path = keyParts[2].ifEmpty { null }
        
        val valueParts = value.split("|")
        require(valueParts.size >= 6) { "Invalid cookie value format" }
        
        val cookieValue = valueParts[0]
        val expiresMillis = valueParts[1].toLongOrNull()
        val secure = valueParts[2] == "1"
        val httpOnly = valueParts[3] == "1"
        val maxAge = valueParts[4].toIntOrNull()
        val encoding = try {
            CookieEncoding.valueOf(valueParts[5])
        } catch (_: Exception) {
            CookieEncoding.URI_ENCODING
        }
        val extensions = if (valueParts.size > 6 && valueParts[6].isNotEmpty()) {
            valueParts[6].split(",").mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1].ifEmpty { null }
                else null
            }.toMap()
        } else {
            emptyMap()
        }
        
        return Cookie(
            name = name,
            value = cookieValue,
            encoding = encoding,
            maxAge = maxAge,
            expires = expiresMillis?.let { GMTDate(it) },
            domain = domain,
            path = path,
            secure = secure,
            httpOnly = httpOnly,
            extensions = extensions
        )
    }

    /**
     * Check if a cookie matches the given URL
     */
    private fun cookieMatchesUrl(cookie: Cookie, url: Url): Boolean {
        val host = url.host
        val domainOk = cookie.domain?.let { domain ->
            host.endsWith(domain.trimStart('.'))
        } ?: true
        val pathOk = cookie.path?.let { path ->
            url.encodedPath.startsWith(path)
        } ?: true
        return domainOk && pathOk
    }
}
