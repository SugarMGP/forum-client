package org.jh.forum.client.di

import io.ktor.client.plugins.cookies.*

expect fun provideCookiesStorage(): CookiesStorage