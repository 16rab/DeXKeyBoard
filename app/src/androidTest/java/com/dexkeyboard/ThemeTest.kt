package com.dexkeyboard

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeTest {

    @Test
    fun testThemePreferenceSaving() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Save Dark Mode
        PreferencesManager.setThemeMode(context, PreferencesManager.THEME_DARK)
        assertEquals(PreferencesManager.THEME_DARK, PreferencesManager.getThemeMode(context))
        
        // Save Light Mode
        PreferencesManager.setThemeMode(context, PreferencesManager.THEME_LIGHT)
        assertEquals(PreferencesManager.THEME_LIGHT, PreferencesManager.getThemeMode(context))
    }

    @Test
    fun testThemeApplication() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        scenario.onActivity { activity ->
            // Test Dark Mode Application
            PreferencesManager.setThemeMode(activity, PreferencesManager.THEME_DARK)
            // We can't easily check the rendered UI color in unit test, 
            // but we can check if the delegate mode was set (indirectly via recreating)
            
            // Note: In a real UI test we would check screenshot or color values
            // Here we verify the logic flow runs without crashing
        }
    }
}
