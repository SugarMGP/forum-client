package org.jh.forum.client.data.storage

import com.russhwolf.settings.Settings
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toOkioPath
import java.nio.file.Files
import java.nio.file.Paths

/**
 * JVM implementation of Settings provider using DataStore.
 * Creates Settings instances backed by DataStore following platform-specific storage conventions:
 * - macOS: ~/Library/Application Support/ForumClient/
 * - Linux: ~/.local/share/ForumClient/
 * - Windows: %APPDATA%/ForumClient/
 * 
 * This uses DataStore which is compatible with the existing storage.
 */
actual fun createSettings(name: String): Settings {
    val dataDir = getDataStoreDirectory()
    try {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir)
        }
    } catch (_: Exception) {
        // Ignore directory creation errors
    }

    val fileName = if (name.endsWith(".preferences_pb")) name else "$name.preferences_pb"
    
    val dataStore = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            dataDir.resolve(fileName).toAbsolutePath().toOkioPath()
        }
    )
    
    return DataStoreSettingsImpl(dataStore)
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
