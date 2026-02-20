package com.dexkeyboard

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "dex_keyboard_prefs"
    private const val KEY_AUTO_SWITCH = "auto_switch"
    private const val KEY_TARGET_IME = "target_ime"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isAutoSwitchEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_SWITCH, false)
    }

    fun setAutoSwitchEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_SWITCH, enabled).apply()
    }

    fun getTargetImeId(context: Context): String? {
        return getPrefs(context).getString(KEY_TARGET_IME, null)
    }

    fun setTargetImeId(context: Context, imeId: String) {
        getPrefs(context).edit().putString(KEY_TARGET_IME, imeId).apply()
    }
}
