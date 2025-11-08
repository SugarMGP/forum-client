package org.jh.forum.client.di

import io.ktor.client.plugins.cookies.*
import org.jh.forum.client.data.storage.DataStoreCookiesStorage
import org.jh.forum.client.data.storage.createDataStore

actual fun provideCookiesStorage(): CookiesStorage {
    return DataStoreCookiesStorage(createDataStore("cookies.preferences_pb"))
}