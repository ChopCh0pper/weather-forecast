package com.example.weatherforecast.API

import org.json.JSONObject
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("forecast.json?aqi=no&alerts=no")
    suspend fun getWeatherForecast(
        @Query("key") key: String,
        @Query("q") q: String,
        @Query("days") days: String
    ) : String
}