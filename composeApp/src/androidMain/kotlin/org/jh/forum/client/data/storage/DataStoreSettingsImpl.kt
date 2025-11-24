package org.jh.forum.client.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Settings implementation backed by DataStore Preferences.
 * This provides a bridge between DataStore and multiplatform-settings API.
 * 
 * Note: This implementation uses `runBlocking` to provide synchronous access to DataStore,
 * which is asynchronous by nature. This may block the calling thread briefly.
 * Consider this trade-off when using Settings methods from performance-critical code.
 */
class DataStoreSettingsImpl(
    private val dataStore: DataStore<Preferences>
) : Settings {
    
    override val keys: Set<String>
        get() = runBlocking {
            dataStore.data.first().asMap().keys.map { it.name }.toSet()
        }
    
    override val size: Int
        get() = runBlocking {
            dataStore.data.first().asMap().size
        }
    
    override fun clear() {
        runBlocking {
            dataStore.edit { it.clear() }
        }
    }
    
    override fun remove(key: String) {
        runBlocking {
            dataStore.edit {
                it.remove(stringPreferencesKey(key))
                it.remove(intPreferencesKey(key))
                it.remove(longPreferencesKey(key))
                it.remove(floatPreferencesKey(key))
                it.remove(doublePreferencesKey(key))
                it.remove(booleanPreferencesKey(key))
            }
        }
    }
    
    override fun hasKey(key: String): Boolean = runBlocking {
        dataStore.data.first().contains(stringPreferencesKey(key)) ||
        dataStore.data.first().contains(intPreferencesKey(key)) ||
        dataStore.data.first().contains(longPreferencesKey(key)) ||
        dataStore.data.first().contains(floatPreferencesKey(key)) ||
        dataStore.data.first().contains(doublePreferencesKey(key)) ||
        dataStore.data.first().contains(booleanPreferencesKey(key))
    }
    
    override fun putInt(key: String, value: Int) {
        runBlocking {
            dataStore.edit { it[intPreferencesKey(key)] = value }
        }
    }
    
    override fun getInt(key: String, defaultValue: Int): Int = runBlocking {
        dataStore.data.first()[intPreferencesKey(key)] ?: defaultValue
    }
    
    override fun getIntOrNull(key: String): Int? = runBlocking {
        dataStore.data.first()[intPreferencesKey(key)]
    }
    
    override fun putLong(key: String, value: Long) {
        runBlocking {
            dataStore.edit { it[longPreferencesKey(key)] = value }
        }
    }
    
    override fun getLong(key: String, defaultValue: Long): Long = runBlocking {
        dataStore.data.first()[longPreferencesKey(key)] ?: defaultValue
    }
    
    override fun getLongOrNull(key: String): Long? = runBlocking {
        dataStore.data.first()[longPreferencesKey(key)]
    }
    
    override fun putString(key: String, value: String) {
        runBlocking {
            dataStore.edit { it[stringPreferencesKey(key)] = value }
        }
    }
    
    override fun getString(key: String, defaultValue: String): String = runBlocking {
        dataStore.data.first()[stringPreferencesKey(key)] ?: defaultValue
    }
    
    override fun getStringOrNull(key: String): String? = runBlocking {
        dataStore.data.first()[stringPreferencesKey(key)]
    }
    
    override fun putFloat(key: String, value: Float) {
        runBlocking {
            dataStore.edit { it[floatPreferencesKey(key)] = value }
        }
    }
    
    override fun getFloat(key: String, defaultValue: Float): Float = runBlocking {
        dataStore.data.first()[floatPreferencesKey(key)] ?: defaultValue
    }
    
    override fun getFloatOrNull(key: String): Float? = runBlocking {
        dataStore.data.first()[floatPreferencesKey(key)]
    }
    
    override fun putDouble(key: String, value: Double) {
        runBlocking {
            dataStore.edit { it[doublePreferencesKey(key)] = value }
        }
    }
    
    override fun getDouble(key: String, defaultValue: Double): Double = runBlocking {
        dataStore.data.first()[doublePreferencesKey(key)] ?: defaultValue
    }
    
    override fun getDoubleOrNull(key: String): Double? = runBlocking {
        dataStore.data.first()[doublePreferencesKey(key)]
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        runBlocking {
            dataStore.edit { it[booleanPreferencesKey(key)] = value }
        }
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = runBlocking {
        dataStore.data.first()[booleanPreferencesKey(key)] ?: defaultValue
    }
    
    override fun getBooleanOrNull(key: String): Boolean? = runBlocking {
        dataStore.data.first()[booleanPreferencesKey(key)]
    }
}
