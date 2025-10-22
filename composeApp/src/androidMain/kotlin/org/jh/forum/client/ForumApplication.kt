package org.jh.forum.client

import android.app.Application

/**
 * Custom Application class for the Forum Client.
 *
 * Provides a singleton instance to access the application context
 * from anywhere in the app, used primarily by AndroidCookiesStorage
 * to initialize DataStore.
 */
class ForumApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ForumApplication
            private set
    }
}
