package com.wydgettech.contextualwalls.ui.upload

import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import android.view.autofill.AutofillId
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.DocumentReference
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wydgettech.contextualwalls.Utility
import kotlin.coroutines.coroutineContext


class UploadViewModel : ViewModel() {

    private var db: FirebaseFirestore
    private var mStorageRef: StorageReference? = null

    init {
        db = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    fun uploadImage(context: Context, uri: Uri, uploadCallback: (Boolean) -> Unit) {
        val ref = mStorageRef!!.child("images/" + uri.toString())
        val uploadTask = ref.putFile(uri)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                uploadLink(context, downloadUri!!, uploadCallback)
                Log.d("uri", downloadUri.toString())
            } else {
                uploadCallback(false)
            }
        }
    }


    fun uploadLink(context: Context, link: Uri, uploadCallback: (Boolean) -> Unit) {
        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                var city = Utility.gpsToCity(context, location!!.latitude, location!!.longitude)
                var arr = hashMapOf(
                    "link" to link.toString()
                )
                db.collection(city).document(link.toString().takeLast(20))
                    .set(arr as Map<String, Any>)
                    .addOnSuccessListener(OnSuccessListener<Void> { documentReference ->
                        Log.d(
                            "Walls",
                            "DocumentSnapshot added with ID: "
                        )
                        uploadCallback(true)
                    })
                    .addOnFailureListener(OnFailureListener { e ->
                        Log.w("Walls", "Error adding document", e)
                        uploadCallback(false)
                    })
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed location gps", Toast.LENGTH_SHORT).show()
            }


    }
}