package com.ksc.movingalarm

import android.Manifest
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/*
    TO DO :
        * GPS set
        * Alarm set
            1) Alarm on/off
            2) vibrator
        * Alarm off
*/

// const val EXTRA_MESSAGE = "main.open.setting"
const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 11

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    private val daysID :Array<Int> by lazy {
        arrayOf(R.id.sun,R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri, R.id.sat)
    }

    val myAlarm :Alarm by lazy {
        Alarm(this)
    }

    private lateinit var mMap : GoogleMap
    private lateinit var destMarker: Marker

    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.main_map) as SupportMapFragment

        mapFragment.getMapAsync {
            onMapReady(it)
        }
        Log.e("map", "sync")

    }

    fun addGeofence () {

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        val geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val geofencingRequest = GeofencingRequest.Builder().apply {
            addGeofence(
                Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this geofence.
                    .setRequestId("destination")
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                        myAlarm.latitude,
                        myAlarm.longitude,
                        10f
                    )
                    // Set the expiration duration of the geofence. This geofence gets automatically removed after this period of time.
                    .setExpirationDuration(1000 * 60 * 30)

                    // Set the transition types of interest. Alerts are only generated for these transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                    // Create the geofence.
                    .build()
            )
        }.build()

        geofencingClient = LocationServices.getGeofencingClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

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

        }//permission

    }

    override fun onMapReady(map: GoogleMap) {
        Log.e("map", "ready")
        mMap = map
        map.setOnMapLongClickListener {
            destMarker.position = it
            myAlarm.longitude = it.longitude
            myAlarm.latitude = it.latitude
        }
        checkPermission()
    }

    private fun checkPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

            Log.e("permission","not")

            // Should we show an explanation?
            if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ) {
                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response!
                // After the user sees the explanation, try again to request the permission.
                Log.e("permission","show")
            } else {
                // No more explanation needed, but we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                Log.e("permission","no show")
            }

        } else {
            // Permission has already been granted
            Log.e("permission","already granted")
            initLocation()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted! Do the contacts-related task you need to do.
                    Log.e("permission","granted")
                    initLocation()
                } else {
                    // permission denied! Disable the functionality that depends on this permission.
                    Log.e("permission","denied")
                    finish()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun initLocation() {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("permission","not")
                checkPermission()
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        Log.e("location","fail")
                    } else {
                        val now = if (myAlarm.latitude * myAlarm.longitude < 1 ) { // 초기값 0 0 이 들어있으면 장치 현재 위치 아니면 마커 위치로
                            LatLng(location.latitude, location.longitude)
                        } else {
                            LatLng(myAlarm.latitude, myAlarm.longitude)
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now,16f))
                        Log.e("camera position : ", "${mMap.cameraPosition}")

                        val dest = LatLng(myAlarm.latitude, myAlarm.longitude)
                        destMarker = mMap.addMarker(MarkerOptions().position(dest).title("dest"))
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()

        set_time.text = "${myAlarm.hour} " + if (myAlarm.minute < 10 ) ": 0${myAlarm.minute}" else ": ${myAlarm.minute}"

        for (i in 0..6) {
            if (myAlarm.dayCheck[i]) {
                findViewById<TextView>(daysID[i]).setTextColor(getColor(R.color.colorAccent))
            } else {
                findViewById<TextView>(daysID[i]).setTextColor(getColor(R.color.fontBlack))
            }
        }

        on_switch.isChecked = myAlarm.onOFF

        on_switch.setOnCheckedChangeListener { _, isChecked ->
            myAlarm.onOFF = isChecked
            myAlarm.save()
        }
    }

    fun showTimePickerDialog(view: View) {
        val c = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minutes ->
                myAlarm.hour = hourOfDay
                myAlarm.minute = minutes
                set_time.text = "${myAlarm.hour} " + if (myAlarm.minute < 10 ) ": 0${myAlarm.minute}" else ": ${myAlarm.minute}"
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(this)
        )
        timePickerDialog.show()
    }

    fun setDays (view: View) {
        var day = -1
        when (view.id) {
            daysID[0] -> day = 0
            daysID[1] -> day = 1
            daysID[2] -> day = 2
            daysID[3] -> day = 3
            daysID[4] -> day = 4
            daysID[5] -> day = 5
            daysID[6] -> day = 6
        }

        myAlarm.dayCheck[day] = !myAlarm.dayCheck[day]

        if (myAlarm.dayCheck[day]) {
            findViewById<TextView>(view.id).setTextColor(getColor(R.color.colorAccent))
        } else {
            findViewById<TextView>(view.id).setTextColor(getColor(R.color.fontBlack))
        }
    }

    fun test (view: View) {

    }

}
