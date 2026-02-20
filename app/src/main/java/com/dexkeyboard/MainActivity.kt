package com.dexkeyboard

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dexkeyboard.databinding.ActivityMainBinding
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val homeFragment = HomeFragment()
    val settingsFragment = SettingsFragment()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        if (ShizukuManager.hasPermission()) {
            homeFragment.refreshStatus()
        } else {
            ShizukuManager.requestPermission(this)
        }
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuManager.REQUEST_CODE) {
            homeFragment.refreshStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ShizukuManager.addBinderReceivedListener(binderReceivedListener)
        ShizukuManager.addRequestPermissionResultListener(requestPermissionResultListener)

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
        ShizukuManager.removeBinderReceivedListener(binderReceivedListener)
        ShizukuManager.removeRequestPermissionResultListener(requestPermissionResultListener)
    }
}
