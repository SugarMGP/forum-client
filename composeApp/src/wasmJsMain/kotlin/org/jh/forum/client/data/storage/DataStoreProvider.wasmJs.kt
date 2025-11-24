package org.jh.forum.client.data.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

/**
 * WasmJs implementation of Settings provider using StorageSettings.
 * Creates Settings instances backed by browser's localStorage.
 * 
 * StorageSettings uses the Web Storage API which persists data in the browser.
 */
actual fun createSettings(name: String): Settings = StorageSettings()
