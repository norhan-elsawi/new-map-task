package com.ibtikar.firstkotlinapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignIn : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()
    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        if (mAuth.currentUser != null) {
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Constants.EMAIL, mAuth.currentUser?.email)
            intent.putExtra(Constants.ID, mAuth.currentUser?.uid)
            startActivity(intent)
            finish()
        }

        login.setOnClickListener { view ->
            if (email.text.toString() != "" && password.text.toString() != "") {
                progressBar.visibility = View.VISIBLE
                mAuth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                        .addOnCompleteListener(this, { task ->
                            if (task.isSuccessful) {
                                var intent = Intent(this, MainActivity::class.java)
                                intent.putExtra(Constants.EMAIL, mAuth.currentUser?.email)
                                intent.putExtra(Constants.ID, mAuth.currentUser?.uid)
                                val prefs = this.getSharedPreferences(Constants.PrefName, 0)
                                val editor = prefs!!.edit()
                                editor.putString(Constants.ID, mAuth.currentUser?.uid)
                                editor.apply()
                                progressBar.visibility = View.GONE
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

        signUp.setOnClickListener { view ->
            var intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()

        }


    }
}
