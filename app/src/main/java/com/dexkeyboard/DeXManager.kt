package com.dexkeyboard

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import android.util.Log

/**
 * DeX 模式检测管理器
 * 提供多种策略检测设备是否处于 Samsung DeX 模式
 */
object DeXManager {
    private const val TAG = "DeXManager"

    /**
     * 检测是否处于 DeX 模式
     * @param context 上下文
     * @return 如果处于 DeX 模式返回 true，否则返回 false
     */
    fun isDeXMode(context: Context): Boolean {
        val config = context.resources.configuration

        // 检查 1: Configuration.uiMode
        // 当处于 DeX 模式时，uiMode 通常会包含 UI_MODE_TYPE_DESK
        if ((config.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_DESK) {
            Log.d(TAG, "通过 UI_MODE_TYPE_DESK 检测到 DeX 模式")
            return true
        }

        // 检查 2: Settings.Global "dex_mode" 或类似键值
        // 注意：键名可能因 Samsung OneUI 版本而异。
        // "dexon" 常用。或者 "desktop_mode"。
        // 用户建议使用 "dex_mode"。
        try {
            // 尝试已知的键名
            val keys = listOf("dexon", "desktop_mode", "dex_mode")
            for (key in keys) {
                val value = Settings.System.getInt(context.contentResolver, key, -1)
                if (value == 1) {
                    Log.d(TAG, "通过 Settings.System.$key == 1 检测到 DeX 模式")
                    return true
                }
                // 同时也检查 Global 设置
                val globalValue = Settings.Global.getInt(context.contentResolver, key, -1)
                if (globalValue == 1) {
                    Log.d(TAG, "通过 Settings.Global.$key == 1 检测到 DeX 模式")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "检查系统设置时出错", e)
        }

        // 检查 3: 反射获取 Samsung 特有的配置
        try {
            val configClass = config.javaClass
            val semDesktopModeEnabledField = configClass.getField("semDesktopModeEnabled")
            val semDesktopModeEnabled = semDesktopModeEnabledField.getInt(config)
            
            val desktopModeEnabledField = configClass.getField("SEM_DESKTOP_MODE_ENABLED")
            val desktopModeEnabled = desktopModeEnabledField.getInt(null)

            if (semDesktopModeEnabled == desktopModeEnabled) {
                Log.d(TAG, "通过 semDesktopModeEnabled 检测到 DeX 模式")
                return true
            }
        } catch (e: Exception) {
            // 忽略反射错误
        }

        return false
    }
}
