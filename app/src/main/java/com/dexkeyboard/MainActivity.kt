package com.dexkeyboard

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dexkeyboard.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

/**
 * Main Activity
 * Consolidated single-page UI for DeX Keyboard Manager
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Shizuku Listeners
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuManager.REQUEST_CODE) {
            checkShizukuStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI
        setupStatusCards()
        setupAutomationSettings()
        setupImeList()
        setupLog()

        // Register Shizuku listeners
        ShizukuManager.addBinderReceivedListener(binderReceivedListener)
        ShizukuManager.addRequestPermissionResultListener(requestPermissionResultListener)
        
        // Initial check
        refreshAll()
    }

    private fun refreshAll() {
        checkShizukuStatus()
        checkDeXStatus()
        loadInputMethods()
    }

    private fun setupStatusCards() {
        binding.cardShizuku.setOnClickListener {
            if (!ShizukuManager.isShizukuAvailable()) {
                log(getString(R.string.log_shizuku_not_running))
                return@setOnClickListener
            }
            if (!ShizukuManager.hasPermission()) {
                ShizukuManager.requestPermission(this)
            } else {
                log(getString(R.string.log_shizuku_granted))
            }
        }

        binding.cardDex.setOnClickListener {
            checkDeXStatus()
            log(getString(R.string.log_dex_refreshed))
        }
    }

    private fun checkShizukuStatus() {
        if (ShizukuManager.isShizukuAvailable()) {
            if (ShizukuManager.hasPermission()) {
                binding.tvShizukuStatus.text = getString(R.string.status_shizuku_running)
                binding.tvShizukuStatus.setTextColor(getColor(R.color.status_success))
            } else {
                binding.tvShizukuStatus.text = getString(R.string.status_shizuku_denied)
                binding.tvShizukuStatus.setTextColor(getColor(R.color.status_error))
            }
        } else {
            binding.tvShizukuStatus.text = getString(R.string.status_shizuku_not_running)
            binding.tvShizukuStatus.setTextColor(getColor(R.color.status_error))
        }
    }

    private fun checkDeXStatus() {
        if (DeXManager.isDeXMode(this)) {
            binding.tvDexStatus.text = getString(R.string.status_dex_active)
            binding.tvDexStatus.setTextColor(getColor(R.color.status_success))
        } else {
            binding.tvDexStatus.text = getString(R.string.status_dex_inactive)
            binding.tvDexStatus.setTextColor(getColor(R.color.text_secondary))
        }
    }

    private fun setupAutomationSettings() {
        // Auto Switch Toggle
        binding.switchAutoDex.isChecked = PreferencesManager.isAutoSwitchEnabled(this)
        binding.switchAutoDex.setOnCheckedChangeListener { _, isChecked ->
            PreferencesManager.setAutoSwitchEnabled(this, isChecked)
            log(if (isChecked) getString(R.string.log_auto_switch_enabled) else getString(R.string.log_auto_switch_disabled))
        }

        // Test Switch Button
        binding.btnTestSwitch.setOnClickListener {
            val selectedPosition = binding.spinnerTargetIme.selectedItemPosition
            val adapter = binding.spinnerTargetIme.adapter
            if (adapter != null && adapter.count > 0 && selectedPosition >= 0) {
                 val imes = ImeManager.getAllInputMethods(this)
                 if (selectedPosition < imes.size) {
                     val targetId = imes[selectedPosition].id
                     switchIme(targetId)
                 }
            } else {
                log(getString(R.string.log_no_target_ime))
            }
        }
    }

    private fun setupImeList() {
        binding.recyclerViewImes.layoutManager = LinearLayoutManager(this)
        // Adapter will be set in loadInputMethods
    }
    
    private fun loadInputMethods() {
        try {
            val imes = ImeManager.getAllInputMethods(this)
            if (imes.isEmpty()) {
                log(getString(R.string.log_no_imes_found))
                return
            }

            // Setup Spinner
            val imeNames = imes.map { it.loadLabel(packageManager).toString() }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, imeNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerTargetIme.adapter = adapter

            // Restore Spinner Selection
            val savedTarget = PreferencesManager.getTargetImeId(this)
            if (savedTarget != null) {
                val index = imes.indexOfFirst { it.id == savedTarget }
                if (index >= 0) {
                    binding.spinnerTargetIme.setSelection(index)
                }
            }

            // Spinner Selection Listener
            binding.spinnerTargetIme.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0 && position < imes.size) {
                        PreferencesManager.setTargetImeId(this@MainActivity, imes[position].id)
                    }
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

            // Setup List
            val currentIme = ImeManager.getCurrentInputMethod(this)
            val listAdapter = ImeAdapter(this, imes, currentIme) { imeId ->
                switchIme(imeId)
            }
            binding.recyclerViewImes.adapter = listAdapter

        } catch (e: Exception) {
            log("Error loading IMEs: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun switchIme(imeId: String) {
        if (!ShizukuManager.hasPermission()) {
            log(getString(R.string.log_permission_required))
            ShizukuManager.requestPermission(this)
            return
        }

        log(getString(R.string.log_switching_to, imeId))
        lifecycleScope.launch(Dispatchers.IO) {
            val success = ImeManager.switchInputMethod(imeId)
            withContext(Dispatchers.Main) {
                if (success) {
                    log(getString(R.string.log_switch_success))
                    loadInputMethods() // Refresh "Current" status
                } else {
                    log(getString(R.string.log_switch_failed))
                }
            }
        }
    }

    private fun setupLog() {
        binding.tvLog.text = getString(R.string.log_ready) + "\n"
    }

    private fun log(message: String) {
        binding.tvLog.append("$message\n")
        val scrollAmount = binding.tvLog.layout?.getLineTop(binding.tvLog.lineCount) ?: 0
        if (scrollAmount > binding.tvLog.height) {
            binding.tvLog.scrollTo(0, scrollAmount - binding.tvLog.height)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ShizukuManager.removeBinderReceivedListener(binderReceivedListener)
        ShizukuManager.removeRequestPermissionResultListener(requestPermissionResultListener)
    }
}
