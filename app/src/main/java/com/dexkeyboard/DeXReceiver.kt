package com.dexkeyboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DeX 状态广播接收器
 * 监听 DeX 模式进入事件，并执行自动切换逻辑
 */
class DeXReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("DeXReceiver", "接收到广播: $action")

        val isEnter = action == "com.samsung.android.desktopmode.action.ENTER_DESKTOP_MODE" ||
                      action == "android.app.action.ENTER_DESKTOP_MODE"

        if (isEnter) {
            // 检查是否开启了自动切换
            if (PreferencesManager.isAutoSwitchEnabled(context)) {
                val targetIme = PreferencesManager.getTargetImeId(context)
                if (!targetIme.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        // 检查 Shizuku 状态
                        try {
                            if (ShizukuManager.isShizukuAvailable()) {
                                if (ShizukuManager.hasPermission()) {
                                    Log.d("DeXReceiver", "正在自动切换到输入法: $targetIme")
                                    ImeManager.switchInputMethod(targetIme)
                                } else {
                                    Log.e("DeXReceiver", "缺少 Shizuku 权限，无法自动切换")
                                }
                            } else {
                                Log.e("DeXReceiver", "Shizuku 服务不可用，无法自动切换")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}
