package org.jh.forum.client.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.jh.forum.client.ForumApplication

/**
 * Android implementation of DataStore provider.
 * Creates DataStore instances in the app's files directory.
 */
actual fun createDataStore(fileName: String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            ForumApplication.instance.filesDir.resolve(fileName).absolutePath.toPath()
        }
    )
}
