package com.wydgettech.contextualwalls.ui.home

import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.wydgettech.contextualwalls.GIFWallpaper
import com.wydgettech.contextualwalls.R
import com.wydgettech.contextualwalls.Utility


class HomeFragment : Fragment() {

    private val LIVE_WALLPAPER_WEATHER_CODE: Int = 20
    private val LIVE_WALLPAPER_LOCATION_CODE: Int = 10
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        val textView2: TextView = root.findViewById(R.id.text_weather)
        val weatherwall: Button = root.findViewById(R.id.weatherwalls)
        val locationwall: Button = root.findViewById(R.id.locationwalls)


        homeViewModel.locationText.observe(this, Observer {
            textView.text = "You are currently in " + it
        })
        homeViewModel.tempaerature.observe(this, Observer {
            val temp = it
            homeViewModel.weather.observe(this, Observer {
                textView2.text = "It is currently " + it + " and " + temp
            })
        })

        homeViewModel.getLocation(context!!)

        weatherwall.setOnClickListener {
            val intent = Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            if (Build.VERSION.SDK_INT > 15)
            {
                val pkg = GIFWallpaper::class.java!!.getPackage()!!.getName()
                val cls = GIFWallpaper::class.java!!.getCanonicalName()
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(pkg, cls!!)
                )
            }
            else
            {
                intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            }
            startActivityForResult(intent, LIVE_WALLPAPER_WEATHER_CODE)
        }

        locationwall.setOnClickListener {
            val intent = Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            if (Build.VERSION.SDK_INT > 15)
            {
                val pkg = GIFWallpaper::class.java!!.getPackage()!!.getName()
                val cls = GIFWallpaper::class.java!!.getCanonicalName()
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(pkg, cls!!)
                )
            }
            else
            {
                intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            }
            startActivityForResult(intent, LIVE_WALLPAPER_LOCATION_CODE)
        }

        return root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == LIVE_WALLPAPER_LOCATION_CODE) {
            Toast.makeText(context, "Enjoy location based contextual wallpapers", Toast.LENGTH_SHORT).show()
            Utility.scheduleWalls(context!!, false)
        }
        if(resultCode == RESULT_OK && requestCode == LIVE_WALLPAPER_WEATHER_CODE) {
            Toast.makeText(context, "Enjoy weather based contextual wallpapers", Toast.LENGTH_SHORT).show()
            Utility.scheduleWalls(context!!, true)
        }
    }

    override fun onResume() {
        homeViewModel.getLocation(context!!)
        super.onResume()
    }
}
