package com.example.weatherforecast.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecast.API.WeatherData
import com.example.weatherforecast.MainViewModel
import com.example.weatherforecast.R
import com.example.weatherforecast.adapters.WeatherAdapter
import com.example.weatherforecast.databinding.FragmentDaysBinding

class DaysFragment : Fragment(), WeatherAdapter.Listener {
    private lateinit var binding: FragmentDaysBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.liveDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        initRcView()
    }

    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(this@DaysFragment)
        rcView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = DaysFragment()
    }

    override fun onClick(item: WeatherData) {
        model.liveDataCurrent.value = item
    }
}