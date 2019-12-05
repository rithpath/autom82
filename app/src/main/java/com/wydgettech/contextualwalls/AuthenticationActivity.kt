package com.wydgettech.contextualwalls

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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

        if(FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, BaseActivity::class.java)
            startActivity(intent)
        }
        request_button.setOnClickListener {
            authenticateNumber(phone_number_et.text.toString())
        }
    }


    private fun authenticateNumber (num: String) {

        Toast.makeText(this, "We are sending you a verification code to " + num, Toast.LENGTH_SHORT).show()

        var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(applicationContext, "Phone number format is invalid.", Toast.LENGTH_LONG).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(applicationContext, "Sorry, you have made too many requests to firebase", Toast.LENGTH_LONG).show()
                }

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
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
            "+1" + num, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks

    }


    private fun signIn(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    val intent = Intent(this, BaseActivity::class.java)
                    startActivity(intent)
                    // ...
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "The code entered was invalid", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}
