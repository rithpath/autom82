package com.wydgettech.contextualwalls.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query



interface WeatherApi {
    @GET("data/2.5/weather?")
    fun getCurrentWeatherData(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("APPID") app_id: String
    ): Call<WeatherResponse>
}