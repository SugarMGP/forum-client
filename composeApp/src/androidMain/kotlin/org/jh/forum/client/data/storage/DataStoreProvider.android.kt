package org.jh.forum.client.data.storage

import com.russhwolf.settings.Settings
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath
import org.jh.forum.client.ForumApplication

/**
 * Android implementation of Settings provider using DataStore.
 * Creates Settings instances backed by DataStore in the app's files directory.
 * 
 * This uses DataStore which is compatible with the existing storage.
 */
actual fun createSettings(name: String): Settings {
    val fileName = if (name.endsWith(".preferences_pb")) name else "$name.preferences_pb"
    
    val dataStore = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            ForumApplication.instance.filesDir.resolve(fileName).absolutePath.toPath()
        }
    )
    
    return DataStoreSettingsImpl(dataStore)
}
