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
import com.example.weatherforecast.constance.Constance
import com.example.weatherforecast.databinding.FragmentHoursBinding
import org.json.JSONArray
import org.json.JSONObject

class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHoursBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            adapter.submitList(parseHoursData(it))
        }
        initRcView()
    }

    private fun parseHoursData(item: WeatherData): List<WeatherData> {
        val hoursArray = JSONArray(item.hours)
        val list = ArrayList<WeatherData>()

        for (i in 0 until hoursArray.length()) {
            val hour = hoursArray[i] as JSONObject
            val item = WeatherData(
                "",
                hour.getString("time"),
                hour.getJSONObject("condition").getString("text"),
                hour.getString("temp_c") + Constance.DEGREE_C,
                "",
                "",
                hour.getJSONObject("condition").getString("icon"),
                ""
            )
            list.add(item)
        }
        return list
    }

    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(null)
        rcView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}