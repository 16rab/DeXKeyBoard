package com.dexkeyboard

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.os.Build
import androidx.annotation.RequiresApi
import android.annotation.SuppressLint
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 输入法管理器
 * 负责获取已安装的输入法列表和执行切换操作
 */
object ImeManager {

    /**
     * 获取已启用输入法列表
     * @param context 上下文
     * @return 输入法信息列表
     */
    fun getEnabledInputMethods(context: Context): List<InputMethodInfo> {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList
    }

    /**
     * 获取当前默认输入法的 ID
     * @param context 上下文
     * @return 当前输入法 ID 字符串
     */
    fun getCurrentInputMethod(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
    }

    /**
     * 通过 Shizuku 切换输入法
     * @param imeId 目标输入法的 ID
     * @return 切换是否成功
     */
    fun switchInputMethod(imeId: String): Boolean {
        // 检查 Shizuku 服务是否可用
        if (!Shizuku.pingBinder()) {
            return false
        }

        // 检查 Android 版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }

        val command = "ime set $imeId"
        return try {
            // 通过 Shizuku 执行 shell 命令
            val process = safeAccessNewProcess(arrayOf("sh", "-c", command), null, null) ?: return false
            val exitCode = process.waitFor()
            // 可选: 如果需要读取错误输出，可以使用 process.errorStream
            exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @SuppressLint("PrivateApi")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun safeAccessNewProcess(command: Array<String>, env: Array<String>?, dir: String?): Process? {
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(null, command, env, dir) as? Process
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
