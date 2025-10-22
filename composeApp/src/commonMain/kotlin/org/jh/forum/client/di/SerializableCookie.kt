package org.jh.forum.client.di

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class SerializableCookie(
    val name: String,
    val value: String,
    val maxAge: Int? = null,
    val expiresMillis: Long? = null,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val extensions: Map<String, String?> = emptyMap(),
    val encoding: CookieEncoding = CookieEncoding.URI_ENCODING
)