package org.jh.forum.client.util

/**
 * Platform-specific URL opener interface
 */
expect object UrlOpener {
    /**
     * Opens a URL in the system's default browser
     * @param url The URL to open
     * @return true if successful, false otherwise
     */
    fun openUrl(url: String): Boolean
}
