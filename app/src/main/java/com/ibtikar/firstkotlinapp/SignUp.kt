package com.ibtikar.firstkotlinapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : AppCompatActivity() {
    private var mAuth = FirebaseAuth.getInstance()
    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUp_su.setOnClickListener { view ->
            if (email_su.text.toString() != "" && password_su.text.toString() != "") {
                mAuth.createUserWithEmailAndPassword(email_su.text.toString(), password_su.text.toString())
                        .addOnCompleteListener(this, { task ->
                            if (task.isSuccessful) {
                                var intent = Intent(this, MainActivity::class.java)
                                intent.putExtra(Constants.EMAIL, mAuth.currentUser?.email)
                                val prefs = this.getSharedPreferences(Constants.PrefName, 0)
                                val editor = prefs!!.edit()
                                editor.putString(Constants.ID, mAuth.currentUser?.uid)
                                editor.apply()
                                intent.putExtra(Constants.ID, mAuth.currentUser?.uid)
                                startActivity(intent)
                                finish()

                            } else {
                                Constants.showMessage(view, "Error: ${task.exception?.message}")
                            }
                        })

            } else {
                Constants.showMessage(view, "Empty fields")
            }
        }


    }

    override fun onBackPressed() {
        var intent = Intent(this, SignIn::class.java)
        startActivity(intent)
        finish()

    }
}
