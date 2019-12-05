package com.wydgettech.contextualwalls.ui.home

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.wydgettech.contextualwalls.Utility
import com.wydgettech.contextualwalls.api.WeatherResponse
import com.wydgettech.contextualwalls.api.WeatherApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeViewModel : ViewModel() {

    var AppId = "7438dd0b5b52d389ece4f81862cc93ac"

    private val _location = MutableLiveData<String>().apply {
        value = ""
    }
    private val _weather = MutableLiveData<String>().apply {
        value = ""
    }
    private val _temperature = MutableLiveData<String>().apply {
        value = ""
    }
    val locationText: MutableLiveData<String> = _location
    val weather: MutableLiveData<String> = _weather
    val tempaerature: MutableLiveData<String> = _temperature



     fun getLocation(context: Context) {
        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                locationText.value = Utility.gpsToCity(context, location!!.latitude, location.longitude)
                getWeatherData(location.latitude.toString(), location.longitude.toString())
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed location gps", Toast.LENGTH_SHORT).show()
            }

    }

    fun getWeatherData(lat: String, lon: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherApi::class.java)
        val call = service.getCurrentWeatherData(lat, lon, AppId)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(@NonNull call: Call<WeatherResponse>, @NonNull response: Response<WeatherResponse>) {
                if (response.code() === 200) {
                    val weatherResponse = response.body()!!
                    _temperature.value = "" + (((weatherResponse.main.temp - 273.15) * 9 / 5) + 32).toInt()  + " degrees Fahrenheit."
                    var rain = false
                    weatherResponse.weather.forEach {
                        if (it.id > 200 && it.id < 599) {
                            rain = true
                        }
                    }
                    _weather.value = if (rain) {
                        "rainy"
                    }  else if (weatherResponse.clouds.all != null && weatherResponse.clouds.all.compareTo(39) > 0) {
                        "cloudy"
                    } else {
                        "sunny"
                    }
                }
            }

            override fun onFailure(@NonNull call: Call<WeatherResponse>, @NonNull t: Throwable) {
                Log.e("APICall", t.toString())
            }
        })
    }

}