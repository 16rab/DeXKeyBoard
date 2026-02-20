package com.dexkeyboard

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ImeManagerTest {
    @Test
    fun getAllInputMethods_returnsNonEmptyList() {
        // 获取应用上下文
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // 调用我们修复后的方法
        val imes = ImeManager.getAllInputMethods(appContext)
        
        // 验证至少有一个输入法（系统默认的）
        assertTrue("Input method list should not be empty", imes.isNotEmpty())
        
        // 打印获取到的输入法 ID，方便调试
        println("Found ${imes.size} IMEs:")
        imes.forEach { ime ->
            println(" - ${ime.id}")
        }
        
        // 验证系统默认输入法是否在列表中
        val currentImeId = ImeManager.getCurrentInputMethod(appContext)
        if (currentImeId.isNotEmpty()) {
            val containsCurrent = imes.any { it.id == currentImeId }
            assertTrue("List should contain current default IME: $currentImeId", containsCurrent)
        }
    }
    
    @Test
    fun getEnabledInputMethods_returnsNonEmptyList() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val imes = ImeManager.getEnabledInputMethods(appContext)
        
        assertTrue("Enabled input method list should not be empty", imes.isNotEmpty())
    }
}
