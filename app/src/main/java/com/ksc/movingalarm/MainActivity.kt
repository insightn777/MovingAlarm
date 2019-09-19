package com.ksc.movingalarm

import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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

const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 11

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    private val daysID :Array<Int> by lazy {
        arrayOf(R.id.sun, R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri, R.id.sat)
    }

    private val myAlarm :Alarm by lazy {
        Alarm(this)
    }

    private val myMap = Map()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.main_map) as SupportMapFragment

        mapFragment.getMapAsync {
            onMapReady(it)
        }
        Log.e("map", "sync")

    }

    override fun onMapReady(map: GoogleMap) {
        Log.e("map", "ready")
        myMap.mMap = map
        map.setOnMapLongClickListener {
            myAlarm.longitude = it.longitude
            myAlarm.latitude = it.latitude
            myMap.destMarker.position = it
            myMap.addGeofence(myAlarm.latitude, myAlarm.longitude, this)
        }
        myMap.initLocation(myAlarm.latitude, myAlarm.longitude, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted! Do the contacts-related task you need to do.
                    Log.e("permission","granted")
                    myMap.initLocation(myAlarm.latitude, myAlarm.longitude, this)
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
}