package com.ibtikar.firstkotlinapp


import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase


class UserLocationListener : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    var prefs: SharedPreferences? = null
    var userId: String? = null

    override fun onBind(intent: Intent?) = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        prefs = this.getSharedPreferences(Constants.PrefName, 0)
        userId = prefs!!.getString(Constants.ID, "")

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        if (!mGoogleApiClient!!.isConnected || !mGoogleApiClient!!.isConnecting) {
            mGoogleApiClient!!.connect()
        }
        return START_STICKY
    }


    override fun onConnected(p0: Bundle?) {
        createLocationRequest();

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e(TAG, "location failed ")
        stopLocationUpdates()
        stopSelf()
    }


    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            Log.e(TAG, "position: " + location.latitude + ", " + location.longitude + " accuracy: " + location.accuracy)
            var locObject = LocationObject(userId!!, location.latitude, location.longitude)

            val mDatabase = FirebaseDatabase.getInstance().getReference("usersLocations")

            mDatabase.child(userId).setValue(locObject)
        }
    }

    companion object {
        val TAG = "LocationTrackingService"

        val INTERVAL = 5000.toLong() // In milliseconds
        val FASTEST_INTERVAL = 5000.toLong()

    }


    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = INTERVAL
        mLocationRequest?.fastestInterval = FASTEST_INTERVAL
        mLocationRequest?.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "activity destroyed ")
        stopLocationUpdates()
    }

}