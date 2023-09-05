package com.asu.mc.healthcontextmonitor.ui.sensing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asu.mc.healthcontextmonitor.databinding.FragmentHomeBinding

class SensingFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnHeartRate.setOnClickListener {
            val action = SensingFragmentDirections.actionSensingFragmentToCameraFragment()
            findNavController().navigate(action)
        }

        binding.btnRespRate.setOnClickListener {
            val action = SensingFragmentDirections.actionSensingFragmentToRespRateFragment()
            findNavController().navigate(action)
        }
    }
}
