package org.jh.forum.client.util

import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String): Boolean {
    return try {
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(url))
                true
            } else {
                false
            }
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}