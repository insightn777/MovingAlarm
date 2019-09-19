package com.ksc.movingalarm

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class Map () {

    lateinit var mMap :GoogleMap
    lateinit var destMarker: Marker

    private lateinit var geofencingClient: GeofencingClient

    fun initLocation(latitude: Double, longitude: Double, activity: Activity) {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.e("permission","not")

            // Should we show an explanation?
            if ( ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ) {
                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response!
                // After the user sees the explanation, try again to request the permission.
                Log.e("permission","show")
            } else {
                // No more explanation needed, but we can request the permission.
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                Log.e("permission","no show")
            }

        } else {
            // Permission has already been granted
            Log.e("permission","already granted")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        Log.e("location","fail")
                    } else {
                        val now = if (latitude * longitude < 1 ) { // 초기값 0 0 이 들어있으면 장치 현재 위치 아니면 마커 위치로
                            LatLng(location.latitude, location.longitude)
                        } else {
                            LatLng(latitude, longitude)
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now,16f))
                        Log.e("camera position : ", "${mMap.cameraPosition}")

                        val dest = LatLng(latitude, longitude)
                        destMarker = mMap.addMarker(MarkerOptions().position(dest).title("dest"))
                    }
                }
        }
    } // initLocation


    fun addGeofence (latitude :Double, longitude :Double,context: Context) {

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val geofencingRequest = GeofencingRequest.Builder().apply {
            addGeofence(
                Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this geofence.
                    .setRequestId("destination")
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                        latitude,
                        longitude,
                        20f
                    )
                    // Set the expiration duration of the geofence. This geofence gets automatically removed after this period of time.
                    .setExpirationDuration(1000 * 60 * 30)

                    // Set the transition types of interest. Alerts are only generated for these transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                    // Create the geofence.
                    .build()
            )
        }.build()

        geofencingClient = LocationServices.getGeofencingClient(context)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("geofence", "permission not")

        } else {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    Log.e("geofence", "add")
                }
                addOnFailureListener {
                    // Failed to add geofences
                    Log.e("geofence", "add fail")
                }
            }

        } //permission

    }  //addGeofence
}


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.e("geofence", "error")
            return
        }
        // Get the transition type. Test that the reported transition was of interest.
        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            // Send notification and log the transition details.
            Log.e("geofence", "arrive")

        } else {
            // Log the error.
            Log.e("geofence", "transition error")
        }
    }
}