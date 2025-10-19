package org.jh.forum.client.di

import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.util.date.*

expect fun provideCookiesStorage(): CookiesStorage

fun Cookie.toSerializable(): SerializableCookie = SerializableCookie(
    name = name,
    value = value,
    maxAge = maxAge,
    expiresMillis = expires?.timestamp,
    domain = domain,
    path = path,
    secure = secure,
    httpOnly = httpOnly,
    extensions = extensions,
    encoding = encoding
)

fun SerializableCookie.toKtorCookie(): Cookie {
    val expiresDate: GMTDate? = expiresMillis?.let { GMTDate(it) }
    return Cookie(
        name = name,
        value = value,
        encoding = encoding,
        maxAge = maxAge,
        expires = expiresDate,
        domain = domain,
        path = path,
        secure = secure,
        httpOnly = httpOnly,
        extensions = extensions
    )
}