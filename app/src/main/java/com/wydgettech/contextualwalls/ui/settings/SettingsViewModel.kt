package com.wydgettech.contextualwalls.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SettingsViewModel : ViewModel() {

    private val _quickApp = MutableLiveData<String>().apply {
        value = ""
    }

    val locationText: MutableLiveData<String> = _quickApp



}