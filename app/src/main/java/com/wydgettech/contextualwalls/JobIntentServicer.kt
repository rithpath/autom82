package com.wydgettech.contextualwalls

import android.app.job.JobParameters
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Example implementation of a JobIntentService.
 */
class JobIntentServicer : JobIntentService() {
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPref: SharedPreferences
    private val mHandler = Handler()

    override fun onHandleWork(intent: Intent) {
        sharedPref = applicationContext.getSharedPreferences("ContextualWalls", 0)
        Log.d("Walls", "Started background")
        var weather = sharedPref.getBoolean("weather", false)
        if (weather) {
            setWeatherWallpaper(this)
        } else
            setWallpaper(applicationContext)

    }

    fun setWallpaper(context: Context) {
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
                }
                dbCol.addOnFailureListener {
                    Utility.scheduleWalls(context, false)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed location gps", Toast.LENGTH_SHORT).show()
                Utility.scheduleWalls(context, false)
            }

    }


    fun setWeatherWallpaper(context: Context) {
        Utility.getLocation(context) {
            if (it != null) {
                Utility.getWeatherType(it?.latitude.toString(), it.longitude.toString()) { weather, temperature ->
                    sharedPref.edit().putBoolean("static", false).commit()
                    sharedPref.edit().putString("weatherType", weather).commit()
                    val intent = Intent("com.wydgettech.contextualwalls.CHANGEWALLPAPER")
                    intent.putExtra("weather", weather)
                    this.sendBroadcast(intent)
                    Utility.scheduleWalls(context, true)
                }
            }
            else {
                Utility.scheduleWalls(context, true)
                Log.d("no loc", "ollsacd")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // Helper for showing tests
    fun toast(text: CharSequence) {
        mHandler.post {
            Toast.makeText(this@JobIntentServicer, text, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        val JOB_ID = 1000

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, JobIntentServicer::class.java, JOB_ID, work)
        }
    }
}