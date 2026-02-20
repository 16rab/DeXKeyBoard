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

/**
 * 主页 Fragment
 * 显示状态卡片、输入法列表和操作日志
 */
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
        
        // 初始加载状态
        refreshStatus()
    }

    /**
     * 刷新 UI 状态（Shizuku, DeX, 输入法列表）
     */
    fun refreshStatus() {
        if (_binding == null) return

        // 检查 Shizuku 状态
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

        // 检查 DeX 状态
        if (DeXManager.isDeXMode(requireContext())) {
            binding.tvDexStatus.text = getString(R.string.dex_mode_on)
            binding.tvDexStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvDexStatus.text = getString(R.string.dex_mode_off)
            binding.tvDexStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
        }

        // 加载输入法列表
        loadImes()
    }

    private fun loadImes() {
        try {
            // 使用 getAllInputMethods 获取完整列表
            val list = ImeManager.getAllInputMethods(requireContext())
            
            if (list.isEmpty()) {
                log("警告：未能获取到任何输入法。请检查权限设置。")
            }
            
            val current = ImeManager.getCurrentInputMethod(requireContext())
            
            val adapter = ImeAdapter(requireContext(), list, current) { imeId ->
                switchIme(imeId)
            }
            binding.recyclerViewImes.adapter = adapter
        } catch (e: Exception) {
            log("获取输入法列表失败: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun switchIme(imeId: String) {
        if (!ShizukuManager.hasPermission()) {
            ShizukuManager.requestPermission(requireActivity())
            return
        }
        
        log("正在切换输入法到: $imeId...")
        lifecycleScope.launch(Dispatchers.IO) {
            val success = ImeManager.switchInputMethod(imeId)
            withContext(Dispatchers.Main) {
                if (success) {
                    log("切换成功！")
                    loadImes() // 刷新列表以更新“当前使用”状态
                } else {
                    log("切换失败。请检查 Shizuku 是否正常运行。")
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
