package org.jh.forum.client.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jh.forum.client.ForumApplication
import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AndroidCookiesStorage : CookiesStorage {
    
    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create {
            File(ForumApplication.instance.filesDir, "cookies.preferences_pb")
        }
    }
    
    private val mutex = Mutex()
    private val cookiesKey = stringPreferencesKey("cookies_json")
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        mutex.withLock {
            val cookies = loadCookies().toMutableList()
            
            // Remove existing cookie with same name, domain, and path
            cookies.removeAll { 
                it.name == cookie.name && 
                it.domain == cookie.domain && 
                it.path == cookie.path 
            }
            
            // Add new cookie
            cookies.add(cookie.toSerializable())
            
            // Save to DataStore
            saveCookies(cookies)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun get(requestUrl: Url): List<Cookie> {
        return mutex.withLock {
            val nowMillis = Clock.System.now().toEpochMilliseconds()
            val cookies = loadCookies().toMutableList()
            
            // Remove expired cookies
            val validCookies = cookies.filter { cookie ->
                cookie.expiresMillis?.let { it > nowMillis } ?: true
            }
            
            // Save back if we removed any expired cookies
            if (validCookies.size != cookies.size) {
                saveCookies(validCookies)
            }
            
            // Return cookies that match the URL
            validCookies
                .filter { cookieMatchesUrl(it, requestUrl) }
                .map { it.toKtorCookie() }
        }
    }

    override fun close() {
        // DataStore handles cleanup automatically
    }

    private suspend fun loadCookies(): List<SerializableCookie> {
        return try {
            val jsonString = dataStore.data.map { preferences ->
                preferences[cookiesKey] ?: "[]"
            }.first()
            
            if (jsonString.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString<List<SerializableCookie>>(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun saveCookies(cookies: List<SerializableCookie>) {
        try {
            val jsonString = json.encodeToString(cookies)
            dataStore.edit { preferences ->
                preferences[cookiesKey] = jsonString
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cookieMatchesUrl(sc: SerializableCookie, url: Url): Boolean {
        val host = url.host
        val domainOk = sc.domain?.let { domain ->
            host.endsWith(domain.trimStart('.'))
        } ?: true
        val pathOk = sc.path?.let { path ->
            url.encodedPath.startsWith(path)
        } ?: true
        return domainOk && pathOk
    }
}
