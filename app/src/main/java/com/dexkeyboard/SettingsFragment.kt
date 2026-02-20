package com.dexkeyboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dexkeyboard.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Auto switch toggle
        binding.switchAutoDex.isChecked = PreferencesManager.isAutoSwitchEnabled(requireContext())
        binding.switchAutoDex.setOnCheckedChangeListener { _, isChecked ->
            PreferencesManager.setAutoSwitchEnabled(requireContext(), isChecked)
        }

        // Target IME Spinner
        val imes = ImeManager.getEnabledInputMethods(requireContext())
        val imeNames = imes.map { it.loadLabel(requireContext().packageManager).toString() }
        val imeIds = imes.map { it.id }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, imeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTargetIme.adapter = adapter

        // Set selection
        val savedTarget = PreferencesManager.getTargetImeId(requireContext())
        if (savedTarget != null) {
            val index = imeIds.indexOf(savedTarget)
            if (index >= 0) {
                binding.spinnerTargetIme.setSelection(index)
            }
        }

        // Save selection
        binding.spinnerTargetIme.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < imeIds.size) {
                    PreferencesManager.setTargetImeId(requireContext(), imeIds[position])
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Test Switch
        binding.btnTestSwitch.setOnClickListener {
            val selectedPosition = binding.spinnerTargetIme.selectedItemPosition
            if (selectedPosition >= 0 && selectedPosition < imeIds.size) {
                val targetId = imeIds[selectedPosition]
                switchIme(targetId)
            }
        }
    }

    private fun switchIme(imeId: String) {
        if (!ShizukuManager.hasPermission()) {
            Toast.makeText(context, "Shizuku permission required", Toast.LENGTH_SHORT).show()
            ShizukuManager.requestPermission(requireActivity())
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val success = ImeManager.switchInputMethod(imeId)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Switch Success!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Switch Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
