package org.jh.forum.client.di

import io.ktor.client.plugins.cookies.*
import org.jh.forum.client.data.storage.DataStoreCookiesStorage
import org.jh.forum.client.data.storage.createDataStore

expect fun provideCookiesStorage(): CookiesStorage