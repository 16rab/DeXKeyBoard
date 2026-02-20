package com.dexkeyboard

import android.content.Context
import android.content.SharedPreferences

/**
 * 偏好设置管理器
 * 存储和读取用户配置，如是否开启自动切换、目标输入法等
 */
object PreferencesManager {
    private const val PREF_NAME = "dex_keyboard_prefs"
    private const val KEY_AUTO_SWITCH = "auto_switch"
    private const val KEY_TARGET_IME = "target_ime"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 是否开启了 DeX 模式自动切换
     */
    fun isAutoSwitchEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_SWITCH, false)
    }

    fun setAutoSwitchEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_SWITCH, enabled).apply()
    }

    /**
     * 获取用户设置的目标输入法 ID
     */
    fun getTargetImeId(context: Context): String? {
        return getPrefs(context).getString(KEY_TARGET_IME, null)
    }

    fun setTargetImeId(context: Context, imeId: String) {
        getPrefs(context).edit().putString(KEY_TARGET_IME, imeId).apply()
    }
}
