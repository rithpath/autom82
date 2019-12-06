package com.wydgettech.contextualwalls.ui.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wydgettech.contextualwalls.Utility

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



     fun getWeatherAndLocation(context: Context) {
         Utility.getLocation(context) {
             if(it != null) {
                 locationText.value =
                     Utility.gpsToCity(context, it!!.latitude, it.longitude)
                 Utility.getWeatherType(it.latitude.toString(), it.longitude.toString()) {
                     if(it != null) {
                         weather.value = it
                     }
                 }
             }
         }
    }


}