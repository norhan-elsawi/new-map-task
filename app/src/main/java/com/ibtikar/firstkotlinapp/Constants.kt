package com.ibtikar.firstkotlinapp

import android.view.View
import android.widget.Toast


/**
 * Created by norhan.elsawi on 11/22/2017.
 */
class Constants {
    companion object {
        const val EMAIL = "email"
        const val ID = "id"
        const val PrefName = "myPrefs"

        fun showMessage(view: View, message: String) {
            Toast.makeText(view.context, message, Toast.LENGTH_LONG).show()
        }
    }
}