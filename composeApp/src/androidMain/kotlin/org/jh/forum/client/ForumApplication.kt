package org.jh.forum.client

import android.app.Application

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
