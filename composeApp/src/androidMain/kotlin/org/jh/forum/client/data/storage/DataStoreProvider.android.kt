package org.jh.forum.client.data.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.datastore.DataStoreSettings
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath
import org.jh.forum.client.ForumApplication

/**
 * Android implementation of Settings provider using DataStoreSettings.
 * Creates Settings instances backed by DataStore in the app's files directory.
 * 
 * This uses DataStoreSettings which is compatible with the existing DataStore storage.
 */
@OptIn(ExperimentalSettingsImplementation::class)
actual fun createSettings(name: String): Settings {
    val fileName = if (name.endsWith(".preferences_pb")) name else "$name.preferences_pb"
    
    val dataStore = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            ForumApplication.instance.filesDir.resolve(fileName).absolutePath.toPath()
        }
    )
    
    return DataStoreSettings(dataStore)
}
