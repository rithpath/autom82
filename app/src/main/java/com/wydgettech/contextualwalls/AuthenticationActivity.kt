package com.wydgettech.contextualwalls

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_authentication.*
import java.util.concurrent.TimeUnit

class AuthenticationActivity : AppCompatActivity() {

    var storedVerificationId = ""
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, BaseActivity::class.java)
            startActivity(intent)
        }
        request_button.setOnClickListener {
            authenticateNumber(phone_number_et.text.toString())
        }
    }


    private fun authenticateNumber(num: String) {

        Toast.makeText(this, "We are sending you a verification code to " + num, Toast.LENGTH_SHORT)
            .show()

        var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        applicationContext,
                        "Phone number format is invalid.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(
                        applicationContext,
                        "Sorry, you have made too many requests to firebase",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token

                verify_tv.visibility = View.VISIBLE
                verification_et.visibility = View.VISIBLE
                verify_button.visibility = View.VISIBLE

                verify_button.setOnClickListener {
                    val code = verification_et.text.toString()
                    val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                    signIn(credential)
                }
            }
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+1" + num,
            60,
            TimeUnit.SECONDS,
            this,
            callbacks
        )

    }


    private fun signIn(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkPerms()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "The code entered was invalid", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
    }

    private fun checkPerms () {
        val permissionAccessCoarseLocationApproved = ActivityCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        if (permissionAccessCoarseLocationApproved) {
            val backgroundLocationPermissionApproved = ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

            if (backgroundLocationPermissionApproved) {
                val intent = Intent(this, BaseActivity::class.java)
                startActivity(intent)
            } else {
                // App can only access location in the foreground. Display a dialog
                // warning the user that your app must have all-the-time access to
                // location in order to function properly. Then, request background
                // location.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    1)

            }
        } else {
            // App doesn't have access to the device's location at all. Make full request
            // for permission.
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if((requestCode == 2 || requestCode == 1)) {
            val intent = Intent(this, BaseActivity::class.java)
            startActivity(intent)
        }
    }

}
