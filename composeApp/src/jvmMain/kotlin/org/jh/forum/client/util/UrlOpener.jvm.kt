package org.jh.forum.client.util

import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String) {
    try {
        val desktop = Desktop.getDesktop()
        desktop.browse(URI(url))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}