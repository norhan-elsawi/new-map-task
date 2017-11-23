package com.ibtikar.firstkotlinapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private var mAuth = FirebaseAuth.getInstance()

    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 200

    private val activity = this

    private var myAlertDialog: AlertDialog? = null

    private var flagStartLocation = false

    var prefs: SharedPreferences? = null
    var userId: String? = null

    private val mGpsSwitchStateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action!!.matches("android.location.PROVIDERS_CHANGED".toRegex())) {
                val retVal = checkProvidersEnabled()
                if (!retVal) {
                    displayPromptForEnablingGPS(activity)
                } else {
                    if (myAlertDialog != null && myAlertDialog!!.isShowing) {
                        myAlertDialog!!.dismiss()
                    }
                    if (!flagStartLocation) {
                        flagStartLocation = true
                        preGetLocation()
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = this.getSharedPreferences(Constants.PrefName, 0)
        userId = prefs!!.getString(Constants.ID, "")
        registerReceiver(mGpsSwitchStateReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        logOut.setOnClickListener { view ->
            mAuth.signOut()
            var intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()

        }
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)

        val retVal = checkProvidersEnabled()
        if (!retVal) {
            if (myAlertDialog == null || !myAlertDialog!!.isShowing) {
                displayPromptForEnablingGPS(this)
            }
        } else {
            flagStartLocation = true
            preGetLocation()
        }


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val mDatabase = FirebaseDatabase.getInstance().getReference("usersLocations")
        mDatabase.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mMap.clear()
                Log.w("main", "location changed")
                for (postSnapshot in dataSnapshot.children) {

                    var mLocation = LatLng(postSnapshot.getValue(LocationObject::class.java)!!.latitude,
                            postSnapshot.getValue(LocationObject::class.java)!!.Longitude)

                    if (postSnapshot.getValue(LocationObject::class.java)!!.userId == userId) {
                        Log.w("main", "me")
                        mMap.addMarker(MarkerOptions().position(mLocation)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    } else {
                        Log.w("main", "other")
                        mMap.addMarker(MarkerOptions().position(mLocation)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("main", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }

    override fun onRestart() {
        super.onRestart()
        val retVal = checkProvidersEnabled()
        if (!retVal) {
            if (myAlertDialog == null || !myAlertDialog!!.isShowing) {
                displayPromptForEnablingGPS(this)
            }
        } else {
            flagStartLocation = true
            preGetLocation()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mGpsSwitchStateReceiver)
        this.stopService(Intent(this, UserLocationListener::class.java))
    }

    private fun preGetLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)

        } else {

            val intent = Intent(this, UserLocationListener::class.java)
            startService(intent)

        }
    }


    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                if (locationAccepted) {

                    val intent = Intent(this, UserLocationListener::class.java)
                    startService(intent)
                }
            }

        }


    }

    private fun checkProvidersEnabled(): Boolean {
        var retVal = false
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        if (gps_enabled || network_enabled) {
            retVal = true
        }

        return retVal
    }

    private fun displayPromptForEnablingGPS(activity: Activity) {

        val builder = AlertDialog.Builder(activity)
        val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        val message = "Please open location setting"

        builder.setMessage(message)
                .setPositiveButton("OK",
                        DialogInterface.OnClickListener { d, id ->
                            activity.startActivity(Intent(action))
                        })
                .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { d, id ->
                            d.cancel()
                            activity.finish()
                        })
        builder.setCancelable(false)
        myAlertDialog = builder.create()
        myAlertDialog!!.show()

    }
}