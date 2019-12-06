package com.wydgettech.contextualwalls

import android.app.job.JobService
import android.content.Intent
import android.app.job.JobParameters
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.content.SharedPreferences



class JobServicer : JobService() {
    private lateinit var db: FirebaseFirestore
    lateinit var sharedPref: SharedPreferences

    override fun onStartJob(params: JobParameters): Boolean {
        val mIntent = Intent(this, JobIntentServicer::class.java)
        JobIntentServicer.enqueueWork(this, mIntent)
        jobFinished(params, false)
        return true
    }


    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

}
