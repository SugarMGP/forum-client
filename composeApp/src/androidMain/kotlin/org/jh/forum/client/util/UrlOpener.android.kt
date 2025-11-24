package org.jh.forum.client.util

import android.content.Intent
import androidx.core.net.toUri
import org.jh.forum.client.ForumApplication

actual fun openUrl(url: String) {
    try {
        val context = ForumApplication.instance.applicationContext
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}