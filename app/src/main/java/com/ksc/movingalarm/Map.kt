package com.ksc.movingalarm

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ksc.movingalarm.service.ACTION_SUCCESS
import com.ksc.movingalarm.service.MyIntentService

const val GPS_ON = 123

class Map (private val activity: Activity) {

    lateinit var mMap :GoogleMap
    lateinit var mMarker: Marker
    private var pos = LatLng(0.0,0.0)

    private lateinit var geoFencingClient: GeofencingClient
    private lateinit var geoFencePendingIntent: PendingIntent

    fun createLocationRequest() {

        val locationRequest = LocationRequest.create().apply {
            interval = 100000
            fastestInterval = 50000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val task = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
                    .addOnSuccessListener {
                        initLocation()
                    }
                    .addOnFailureListener {
                        if (it is ResolvableApiException) {
                            try {
                                it.startResolutionForResult(activity, GPS_ON)
                            } catch (sendEx: IntentSender.SendIntentException) {

                            }
                        }
                    }
    }

    fun checkPermission(latitude: Double, longitude: Double) {

        pos = LatLng(latitude,longitude)

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("permission","not granted")
            // Should we show an explanation?
            if ( ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ) {
                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response!
                // After the user sees the explanation, try again to request the permission.
                Log.e("permission","show")
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            } else {
                // No more explanation needed, but we can request the permission.
                Log.e("permission","no show")
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            }

        } else {
            // Permission has already been granted
            Log.e("permission","already granted")
            createLocationRequest()
        }
    }   // checkPermission

    fun initLocation () {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) {
                Log.e("location","fail")
            } else {
                mMap.isMyLocationEnabled = true

                if (pos.latitude * pos.longitude < 1 ) // 초기값 0 0 이 들어있으면 장치 현재 위치 아니면 마커 위치로
                    pos = LatLng(location.latitude, location.longitude)

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,16f))
                Log.e("camera position : ", "${mMap.cameraPosition}")

                mMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("destination")
                )
                Log.e("Marker", mMarker.id)

            }
        }
    }

    fun moveMarker(latLng: LatLng) {
        mMarker.position = latLng
        Log.e("Marker", mMarker.id)
    }


    fun addGeofence (latitude :Double, longitude :Double, limit :Int) {

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
        val intent = Intent(activity, GeoFenceBroadcastReceiver::class.java)
        geoFencePendingIntent = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val geoFencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(
                Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this geofence.
                    .setRequestId("destination")
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                        latitude,
                        longitude,
                        50f
                    )
                    // Set the expiration duration of the geofence. This geofence gets automatically removed after this period of time.
                    .setExpirationDuration(limit * 60 * 1000L)
                    .setLoiteringDelay(2000)
                    // Set the transition types of interest. Alerts are only generated for these transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    // Create the geofence.
                    .build()
            )
        }.build()

        geoFencingClient = LocationServices.getGeofencingClient(activity)

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("geoFence", "permission not")
        } else {
            geoFencingClient.addGeofences(geoFencingRequest, geoFencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    Log.e("geoFence", "add $latitude:$longitude")

                }
                addOnFailureListener {
                    // Failed to add geofences
                    Log.e("geoFence", "add fail")
                }
            }

        } //permission

    }  //addGeoFence

    fun removeGeofence () {
        geoFencingClient.removeGeofences(geoFencePendingIntent).run {
            addOnSuccessListener {
                // Geofences removed
                Log.e("geoFence", "removed")
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.e("geoFence", "removed fail : $it")
            }
        }
    }
}

/********************************

Receiver Class

 ********************************/

class GeoFenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geoFencingEvent = GeofencingEvent.fromIntent(intent)
        if (geoFencingEvent.hasError()) {
            Log.e("geoFence", "error")
            return
        }
        // Get the transition type. Test that the reported transition was of interest.
        if (geoFencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.e("geoFence", "arrive : ${geoFencingEvent.geofenceTransition}")

            Intent(context, MyIntentService::class.java).apply {
                action = ACTION_SUCCESS
            }.also { intent1 ->
                context?.startService(intent1)
            }

        } else {
            // Log the error.
            Log.e("geoFence", "other transition")
        }
    }
}