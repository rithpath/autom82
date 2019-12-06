package com.wydgettech.contextualwalls.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.wydgettech.contextualwalls.R
import android.content.Intent
import android.widget.ToggleButton
import android.content.SharedPreferences
import androidx.lifecycle.Observer


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_wallpapers, container, false)
        val toggle = root.findViewById<ToggleButton>(R.id.toggleButton)
        val quickText = root.findViewById<TextView>(R.id.quick_app_text)
        val quick = root.findViewById<Button>(R.id.quick_app_button)
        sharedPreferences = context!!.getSharedPreferences("ContextualWalls", 0)


        toggle.setOnCheckedChangeListener { compoundButton, isChecked ->
            if(isChecked)
                sharedPreferences.edit().putBoolean("quick", true).apply()
            else
                sharedPreferences.edit().putBoolean("quick", false).apply()
        }

        quick.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK_ACTIVITY
            intent.putExtra(Intent.EXTRA_TITLE, "Launch using")
            this.startActivityForResult(intent, 1)
        }

        settingsViewModel.locationText.observe(this, Observer {
            quickText.text = it
        })
        return root
    }

    override fun onResume() {
        super.onResume()
        val temp = sharedPreferences.getString("package", "")
        if(temp != "")
            settingsViewModel.locationText.postValue("Your quick app package is " + temp)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) {
            val appName = data!!.getComponent()!!.packageName
            sharedPreferences.edit().putString("package", appName).apply()
            settingsViewModel.locationText.postValue("Your quick app package is "  + appName)
        }
    }
}
