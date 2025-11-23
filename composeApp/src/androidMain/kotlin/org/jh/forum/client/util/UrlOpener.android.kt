package org.jh.forum.client.util

import android.content.Intent
import androidx.core.net.toUri

actual fun openUrl(url: String): Boolean {
    return try {
        val context = android.app.Application().applicationContext
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}