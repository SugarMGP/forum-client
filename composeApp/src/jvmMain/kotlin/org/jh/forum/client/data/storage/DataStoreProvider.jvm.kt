package org.jh.forum.client.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.nio.file.Files
import java.nio.file.Paths

/**
 * JVM implementation of DataStore provider.
 * Creates DataStore instances following platform-specific storage conventions:
 * - macOS: ~/Library/Application Support/ForumClient/
 * - Linux: ~/.local/share/ForumClient/
 * - Windows: %APPDATA%/ForumClient/
 */
actual fun createDataStore(fileName: String): DataStore<Preferences> {
    val dataDir = getDataStoreDirectory()
    try {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir)
        }
    } catch (_: Exception) {
        // Ignore directory creation errors
    }

    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            dataDir.resolve(fileName).toAbsolutePath().toString().toPath()
        }
    )
}

/**
 * Get platform-specific data directory following OS conventions
 */
private fun getDataStoreDirectory(): java.nio.file.Path {
    val appName = "ForumClient"
    val userHome = System.getProperty("user.home")
    val osName = System.getProperty("os.name").lowercase()

    return when {
        osName.contains("mac") || osName.contains("darwin") -> {
            // macOS: ~/Library/Application Support/ForumClient/
            Paths.get(userHome, "Library", "Application Support", appName)
        }

        osName.contains("win") -> {
            // Windows: %APPDATA%/ForumClient/
            val appData = System.getenv("APPDATA") ?: Paths.get(userHome, "AppData", "Roaming").toString()
            Paths.get(appData, appName)
        }

        else -> {
            // Linux and others: ~/.local/share/ForumClient/
            val xdgDataHome = System.getenv("XDG_DATA_HOME") ?: Paths.get(userHome, ".local", "share").toString()
            Paths.get(xdgDataHome, appName)
        }
    }
}
