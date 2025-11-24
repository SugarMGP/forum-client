package org.jh.forum.client.util

import kotlinx.browser.window

actual fun openUrl(url: String) {
    try {
        window.open(url, "_blank")
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}