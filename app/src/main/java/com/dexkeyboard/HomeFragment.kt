package com.dexkeyboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dexkeyboard.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.recyclerViewImes.layoutManager = LinearLayoutManager(requireContext())
        
        // Initial load
        refreshStatus()
    }

    fun refreshStatus() {
        if (_binding == null) return

        // Shizuku
        if (ShizukuManager.isShizukuAvailable()) {
            if (ShizukuManager.hasPermission()) {
                binding.tvShizukuStatus.text = getString(R.string.shizuku_permission_granted)
                binding.tvShizukuStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
            } else {
                binding.tvShizukuStatus.text = getString(R.string.shizuku_permission_denied)
                binding.tvShizukuStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
            }
        } else {
            binding.tvShizukuStatus.text = getString(R.string.shizuku_not_running)
            binding.tvShizukuStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
        }

        // DeX
        if (DeXManager.isDeXMode(requireContext())) {
            binding.tvDexStatus.text = getString(R.string.dex_mode_on)
            binding.tvDexStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvDexStatus.text = getString(R.string.dex_mode_off)
            binding.tvDexStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
        }

        loadImes()
    }

    private fun loadImes() {
        val list = ImeManager.getEnabledInputMethods(requireContext())
        val current = ImeManager.getCurrentInputMethod(requireContext())
        
        val adapter = ImeAdapter(requireContext(), list, current) { imeId ->
            switchIme(imeId)
        }
        binding.recyclerViewImes.adapter = adapter
    }

    private fun switchIme(imeId: String) {
        if (!ShizukuManager.hasPermission()) {
            ShizukuManager.requestPermission(requireActivity())
            return
        }
        
        log("Switching to $imeId...")
        lifecycleScope.launch(Dispatchers.IO) {
            val success = ImeManager.switchInputMethod(imeId)
            withContext(Dispatchers.Main) {
                if (success) {
                    log("Switch success!")
                    loadImes() // Refresh list to update "Current" status
                } else {
                    log("Switch failed. Check if Shizuku is running.")
                }
            }
        }
    }

    private fun log(msg: String) {
        if (_binding != null) {
            binding.tvLog.append("$msg\n")
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
