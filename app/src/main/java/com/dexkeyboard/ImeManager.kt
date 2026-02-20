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

import android.content.Intent
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethod

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
     * 获取系统全部输入法列表（包括未启用或被禁用的）
     * 显式使用 PackageManager 查询，确保覆盖所有可能的输入法
     * @param context 上下文
     * @return 完整的输入法信息列表
     */
    fun getAllInputMethods(context: Context): List<InputMethodInfo> {
        val result = ArrayList<InputMethodInfo>()
        val pm = context.packageManager
        
        try {
            val services = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentServices(
                    Intent(android.view.inputmethod.InputMethod.SERVICE_INTERFACE),
                    PackageManager.ResolveInfoFlags.of((PackageManager.MATCH_ALL or PackageManager.GET_META_DATA).toLong())
                )
            } else {
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PackageManager.MATCH_ALL or PackageManager.GET_META_DATA
                } else {
                    PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS
                }
                @Suppress("DEPRECATION")
                pm.queryIntentServices(
                    Intent(android.view.inputmethod.InputMethod.SERVICE_INTERFACE),
                    flags
                )
            }

            for (resolveInfo in services) {
                try {
                    val imi = InputMethodInfo(context, resolveInfo)
                    result.add(imi)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace() // 可能是没有 QUERY_ALL_PACKAGES 权限
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return result
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
