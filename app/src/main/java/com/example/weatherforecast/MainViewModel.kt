package com.example.weatherforecast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherforecast.API.WeatherData

class MainViewModel : ViewModel() {
    val liveDataCurrent = MutableLiveData<WeatherData>()
    val liveDataList = MutableLiveData<List<WeatherData>>()
}