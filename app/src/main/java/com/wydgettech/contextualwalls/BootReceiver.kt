package com.wydgettech.contextualwalls

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Utility.scheduleWalls(context, false)
    }
}
