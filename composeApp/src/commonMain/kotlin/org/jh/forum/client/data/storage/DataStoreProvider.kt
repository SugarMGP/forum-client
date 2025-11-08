package org.jh.forum.client.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Platform-specific DataStore provider.
 * Each platform implements its own way of creating DataStore instances.
 */
expect fun createDataStore(fileName: String): DataStore<Preferences>
