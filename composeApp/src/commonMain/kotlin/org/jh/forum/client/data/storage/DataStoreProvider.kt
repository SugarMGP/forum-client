package org.jh.forum.client.data.storage

import com.russhwolf.settings.Settings

/**
 * Platform-specific Settings provider.
 * Each platform implements its own way of creating Settings instances.
 */
expect fun createSettings(name: String = "app_settings"): Settings

