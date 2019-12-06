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
    lateinit var sharedPref: SharedPreferences

    override fun onStartJob(params: JobParameters): Boolean {
        sharedPref = applicationContext.getSharedPreferences("ContextualWalls", 0)
        Log.d("Walls", "Started background")
        var weather = sharedPref.getBoolean("weather", false)
        if (weather) {
            setWeatherWallpaper(this, params)
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

                    sharedPref.edit().putString("linkURL", link).commit()
                    sharedPref.edit().putBoolean("static", true).commit()

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

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    fun setWeatherWallpaper(context: Context, params: JobParameters) {

        Utility.getLocation(context) {
            if (it != null) {
                Utility.getWeatherType(it?.latitude.toString(), it.longitude.toString()) { weather, temperature ->
                    sharedPref.edit().putBoolean("static", false).commit()
                    sharedPref.edit().putString("weatherType", weather).commit()
                    val intent = Intent("com.wydgettech.contextualwalls.CHANGEWALLPAPER")
                    intent.putExtra("weather", weather)
                    this.sendBroadcast(intent)
                    Utility.scheduleWalls(context, true)
                    jobFinished(params, false)
                }
            }
        }
    }

}
