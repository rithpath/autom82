package com.wydgettech.contextualwalls

import android.app.job.JobService
import android.content.Intent
import android.app.job.JobParameters
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.location.LocationServices
import com.wydgettech.contextualwalls.api.WeatherApi
import com.wydgettech.contextualwalls.api.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class JobServicer : JobService() {
    private lateinit var db: FirebaseFirestore
    var AppId = "7438dd0b5b52d389ece4f81862cc93ac"


    override fun onStartJob(params: JobParameters): Boolean {
        Log.d("Walls", "Started background")
        val sharedPref: SharedPreferences =
            applicationContext.getSharedPreferences("ContextualWalls", 0)
        var weather = sharedPref.getBoolean("weather", false)
        if (weather) {
            getWeatherData(this, params)
        } else
            setWallpaper(params, applicationContext)

        return false
    }

    fun setWallpaper(params: JobParameters, context: Context) {
        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                db = FirebaseFirestore.getInstance()
                var city = Utility.gpsToCity(context, location!!.latitude, location!!.longitude)
                var dbCol = db.collection(city).get()
                dbCol.addOnSuccessListener {
                    var docs: DocumentSnapshot = it.documents[(0 until it.size()).random()]
                    var link = docs.get("link") as String

                    val intent = Intent("com.wydgettech.contextualwalls.CHANGEWALLPAPER")
                    intent.putExtra("link", link)
                    this.sendBroadcast(intent)
                    Toast.makeText(applicationContext, "Loading new wallpaper!", Toast.LENGTH_SHORT)
                        .show()
                    Utility.scheduleWalls(context, false)
                    jobFinished(params, false)
                }
                dbCol.addOnFailureListener {
                    Utility.scheduleWalls(context, false)
                    jobFinished(params, true)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed location gps", Toast.LENGTH_SHORT).show()
                Utility.scheduleWalls(context, false)
                jobFinished(params, true)
            }

    }

    fun setWeatherWallpaper(context: Context, input: String) {
        val intent = Intent("com.wydgettech.contextualwalls.CHANGEWALLPAPER")
        intent.putExtra("weather", input)
        this.sendBroadcast(intent)
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    fun getWeatherData(context: Context, params: JobParameters) {
        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                var latitude = location!!.latitude.toString()
                var longitude = location!!.longitude.toString()
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val service = retrofit.create(WeatherApi::class.java)
                val call = service.getCurrentWeatherData(latitude, longitude, AppId)
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
                                setWeatherWallpaper(context, "rainy")
                            } else if (weatherResponse.clouds.all != null && weatherResponse.clouds.all.compareTo(
                                    39
                                ) > 0
                            ) {
                                setWeatherWallpaper(context, "cloudy")
                            } else {
                                setWeatherWallpaper(context, "sunny")
                            }
                            Utility.scheduleWalls(context, true)
                            jobFinished(params, false)
                        }
                    }

                    override fun onFailure(@NonNull call: Call<WeatherResponse>, @NonNull t: Throwable) {
                        Log.e("APICall", t.toString())
                        Utility.scheduleWalls(context, true)

                        jobFinished(params, true)

                    }
                })
            }
            .addOnFailureListener {
                Utility.scheduleWalls(context, true)
                Toast.makeText(context, "Failed location gps", Toast.LENGTH_SHORT).show()
            }

    }

}
