package com.wydgettech.contextualwalls

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import java.util.*


class Utility {

    companion object {


        // schedule the start of the service every 10 - 30 seconds
        fun scheduleWalls(context: Context, weather: Boolean) {
            val sharedPref: SharedPreferences = context.getSharedPreferences("ContextualWalls", 0)
            sharedPref.edit().putBoolean("weather", weather).apply()

            Toast.makeText(context, "Rescheduling", Toast.LENGTH_SHORT).show()
            val serviceComponent = ComponentName(context!!, JobServicer::class.java)
            val builder = JobInfo.Builder(0, serviceComponent)
            builder.setOverrideDeadline((30 * 1000).toLong()) // For the demo
            builder.setMinimumLatency(20000)

            //builder.setPeriodic(1000 * 60 * 20, 10000) Real life

            val jobScheduler = context!!.getSystemService(JobScheduler::class.java)
            jobScheduler.schedule(builder.build())
        }


        fun getWeather(): String {
            return "cloudy"
        }


        fun gpsToCity(context: Context, latitude: Double, longitude: Double): String {

            val geocoder = Geocoder(context, Locale.ENGLISH)

            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.size > 0) {
                val curr: Address = addresses[0]
                if (curr.locality == null) {
                    return curr.countryName
                }
                return curr.locality
            }
            else
                return "default"
        }

    }

    }


