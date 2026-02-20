package com.dexkeyboard

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dexkeyboard.databinding.ActivityMainBinding
import rikka.shizuku.Shizuku

/**
 * 主 Activity
 * 负责 Shizuku 权限监听和 Fragment 导航
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val homeFragment = HomeFragment()
    val settingsFragment = SettingsFragment()

    // Shizuku 服务绑定监听器
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        if (ShizukuManager.hasPermission()) {
            homeFragment.refreshStatus()
        } else {
            ShizukuManager.requestPermission(this)
        }
    }

    // Shizuku 权限请求结果监听器
    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuManager.REQUEST_CODE) {
            homeFragment.refreshStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 注册 Shizuku 监听器
        ShizukuManager.addBinderReceivedListener(binderReceivedListener)
        ShizukuManager.addRequestPermissionResultListener(requestPermissionResultListener)

        // 设置底部导航栏
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> switchFragment(homeFragment)
                R.id.navigation_settings -> switchFragment(settingsFragment)
            }
            true
        }

        if (savedInstanceState == null) {
            switchFragment(homeFragment)
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除 Shizuku 监听器，防止内存泄漏
        ShizukuManager.removeBinderReceivedListener(binderReceivedListener)
        ShizukuManager.removeRequestPermissionResultListener(requestPermissionResultListener)
    }
}
