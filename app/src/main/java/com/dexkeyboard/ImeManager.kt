package com.dexkeyboard

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ImeManager {

    fun getEnabledInputMethods(context: Context): List<InputMethodInfo> {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList
    }

    fun getCurrentInputMethod(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
    }

    fun switchInputMethod(imeId: String): Boolean {
        if (!Shizuku.pingBinder()) {
            return false
        }

        val command = "ime set $imeId"
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val exitCode = process.waitFor()
            // Optional: Read output for errors if needed
            // val reader = BufferedReader(InputStreamReader(process.inputStream))
            // val output = reader.readText()
            exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
