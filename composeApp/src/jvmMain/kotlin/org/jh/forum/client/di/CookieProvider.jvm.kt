package org.jh.forum.client.di

import io.ktor.client.plugins.cookies.*
import org.jh.forum.client.data.storage.SettingsCookiesStorage
import org.jh.forum.client.data.storage.createSettings

actual fun provideCookiesStorage(): CookiesStorage {
    return SettingsCookiesStorage(createSettings("cookies"))
}