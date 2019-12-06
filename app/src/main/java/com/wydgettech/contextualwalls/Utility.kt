package com.wydgettech.contextualwalls

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import android.util.Log
import androidx.annotation.NonNull
import java.util.*
import com.wydgettech.contextualwalls.api.WeatherApi
import com.wydgettech.contextualwalls.api.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Utility {

    companion object {
        var AppId = "7438dd0b5b52d389ece4f81862cc93ac"


        // schedule the start of the service every 10 - 30 seconds
        fun scheduleWalls(context: Context, weather: Boolean) {
            val sharedPref: SharedPreferences = context.getSharedPreferences("ContextualWalls", 0)
            sharedPref.edit().putBoolean("weather", weather).apply()

            Toast.makeText(context, "Rescheduling", Toast.LENGTH_SHORT).show()
            val serviceComponent = ComponentName(context!!, JobServicer::class.java)
            val builder = JobInfo.Builder(0, serviceComponent)
            builder.setOverrideDeadline((30 * 1000 * 1).toLong()) // For the demo
            builder.setMinimumLatency(20 * 1000 * 1)

            //builder.setPeriodic(1000 * 60 * 20, 10000) Real life

            val jobScheduler = context!!.getSystemService(JobScheduler::class.java)
            jobScheduler.schedule(builder.build())
        }


        fun getLocation(context: Context, callback: (Location?) -> Unit) {
            var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    callback(location)
                }
                .addOnFailureListener {
                    callback(null)
                    Toast.makeText(context, "Failed location gps", Toast.LENGTH_SHORT).show()
                }

        }

        fun gpsToCity(context: Context, latitude: Double, longitude: Double): String {

            val geocoder = Geocoder(context, Locale.ENGLISH)

            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.size > 0) {
                val curr: Address = addresses[0]
                if (curr.locality == null) {
                    if (curr.countryName == null)
                        return "Unknown"
                    return curr.countryName
                }
                return curr.locality
            } else
                return "Unknown"
        }

        fun kelvinToFarenheit(weather: Float): Double {
            return ((weather - 273.15) * 9 / 5) + 32
        }

        fun getWeatherType(lat: String, lon: String, weatherCallback: (String?) -> Unit) {
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
                        var rain = false
                        weatherResponse.weather.forEach {
                            if (it.id > 200 && it.id < 599) {
                                rain = true
                            }
                        }
                        if (rain) {
                            weatherCallback("rainy")
                        } else if (weatherResponse.clouds.all != null && weatherResponse.clouds.all.compareTo(
                                39
                            ) > 0
                        ) {
                            weatherCallback("cloudy")
                        } else {
                            weatherCallback("sunny")
                        }
                    }
                }

                override fun onFailure(@NonNull call: Call<WeatherResponse>, @NonNull t: Throwable) {
                    weatherCallback(null)
                    Log.e("APICall", t.toString())
                }
            })
        }

        fun getWeatherMessage(lat: String, lon: String, weatherCallback: (String?) -> Unit) {
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
                        var rain = false
                        weatherResponse.weather.forEach {
                            if (it.id > 200 && it.id < 599) {
                                rain = true
                            }
                        }
                        var tempString =
                            "It is currently " + kelvinToFarenheit(weatherResponse.main.temp).toInt() + " degrees Fahrenheit"
                        if (rain) {
                            tempString += " and rainy. You might want to carry an umbrella"
                        } else if (weatherResponse.clouds.all != null && weatherResponse.clouds.all.compareTo(
                                39
                            ) > 0
                        ) {
                            tempString += " and cloudy."
                        } else {
                            tempString += " and sunny."
                        }

                        if (weatherResponse.main.temp < 60) {
                            tempString += " and wear a jacket."
                        } else if (weatherResponse.main.temp > 60 && weatherResponse.main.temp < 80) {
                            tempString += "."
                        } else {
                            tempString += " Go light on the layers!"
                        }
                        weatherCallback(tempString)
                    }
                }

                override fun onFailure(@NonNull call: Call<WeatherResponse>, @NonNull t: Throwable) {
                    weatherCallback(null)
                    Log.e("APICall", t.toString())
                }
            })
        }
    }

}


