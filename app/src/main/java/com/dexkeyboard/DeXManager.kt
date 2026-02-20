package com.dexkeyboard

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import android.util.Log

object DeXManager {
    private const val TAG = "DeXManager"

    fun isDeXMode(context: Context): Boolean {
        val config = context.resources.configuration

        // Check 1: Configuration.uiMode
        if ((config.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_DESK) {
            Log.d(TAG, "DeX detected via UI_MODE_TYPE_DESK")
            return true
        }

        // Check 2: Settings.Global "dex_mode" or similar
        // Note: The key might vary by Samsung OneUI version. 
        // "dexon" is commonly used. Or "desktop_mode".
        // The user suggested "dex_mode".
        try {
            // Try known keys
            val keys = listOf("dexon", "desktop_mode", "dex_mode")
            for (key in keys) {
                val value = Settings.System.getInt(context.contentResolver, key, -1)
                if (value == 1) {
                    Log.d(TAG, "DeX detected via Settings.System.$key == 1")
                    return true
                }
                // Also check Global
                val globalValue = Settings.Global.getInt(context.contentResolver, key, -1)
                if (globalValue == 1) {
                    Log.d(TAG, "DeX detected via Settings.Global.$key == 1")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Settings", e)
        }

        // Check 3: Reflection for Samsung specific configuration
        try {
            val configClass = config.javaClass
            val semDesktopModeEnabledField = configClass.getField("semDesktopModeEnabled")
            val semDesktopModeEnabled = semDesktopModeEnabledField.getInt(config)
            
            val desktopModeEnabledField = configClass.getField("SEM_DESKTOP_MODE_ENABLED")
            val desktopModeEnabled = desktopModeEnabledField.getInt(null)

            if (semDesktopModeEnabled == desktopModeEnabled) {
                Log.d(TAG, "DeX detected via semDesktopModeEnabled")
                return true
            }
        } catch (e: Exception) {
            // Ignore reflection errors
        }

        return false
    }
}
