package com.dexkeyboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeXReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("DeXReceiver", "Received action: $action")

        val isEnter = action == "com.samsung.android.desktopmode.action.ENTER_DESKTOP_MODE" ||
                      action == "android.app.action.ENTER_DESKTOP_MODE"

        if (isEnter) {
            if (PreferencesManager.isAutoSwitchEnabled(context)) {
                val targetIme = PreferencesManager.getTargetImeId(context)
                if (!targetIme.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        // Check Shizuku
                        try {
                            if (ShizukuManager.isShizukuAvailable()) {
                                if (ShizukuManager.hasPermission()) {
                                    Log.d("DeXReceiver", "Auto switching to $targetIme")
                                    ImeManager.switchInputMethod(targetIme)
                                } else {
                                    Log.e("DeXReceiver", "Shizuku permission missing")
                                }
                            } else {
                                Log.e("DeXReceiver", "Shizuku not available")
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
