package org.jh.forum.client.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.nio.file.Files
import java.nio.file.Paths

/**
 * JVM implementation of DataStore provider.
 * Creates DataStore instances in the cache directory.
 */
actual fun createDataStore(fileName: String): DataStore<Preferences> {
    val dataDir = Paths.get("cache", "datastore")
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
