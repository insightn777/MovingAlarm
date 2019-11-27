package com.ksc.movingalarm

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.ksc.movingalarm.service.TimeService
import com.ksc.movingalarm.ui.AwakeActivity
import com.ksc.movingalarm.ui.ReportActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ClassCastException
import java.util.*
import javax.xml.transform.ErrorListener

/*
    TO DO :
        * Alarm set
            1) Alarm on/off
            2) vibrator
        * Alarm off
*/

const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 11

class MainActivity : FragmentActivity(), OnMapReadyCallback, NumberPickerFragment.NumberListener {

    private val daysID :Array<Int> by lazy {
        arrayOf(R.id.sun, R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri, R.id.sat)
    }

    private val myAlarm :Alarm by lazy {
        Alarm(this)
    }

    private val myMap = Map(this)

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
        with (myMap) {
            mMap = map.apply {
                setOnMapLongClickListener {
                    myAlarm.longitude = it.longitude
                    myAlarm.latitude = it.latitude
                    myMap.moveMarker(it)
                }
                uiSettings.isMyLocationButtonEnabled = true
            }
            checkPermission(myAlarm.latitude, myAlarm.longitude)
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
                    myMap.checkPermission(myAlarm.latitude, myAlarm.longitude)
                } else {
                    // permission denied! Disable the functionality that depends on this permission.
                    Log.e("permission","denied")
                    finish()
                }
                return
            }
            // Add other 'when' lines to check for other permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val s = if (myAlarm.minute < 10 ) "%d : 0%d" else "%d : %d"
        set_time.text = String.format(s, myAlarm.hour,myAlarm.minute)
        limit_time.text = String.format("%d min",myAlarm.limitTime)

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
                val s = if (myAlarm.minute < 10 ) "%d : 0%d" else "%d : %d"
                set_time.text = String.format(s, myAlarm.hour,myAlarm.minute)
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(this)
        )
        timePickerDialog.show()
    }

    fun showNumberPickerDialog(view: View) {
        NumberPickerFragment(this).show(supportFragmentManager,"number")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, limitTime: Int) {
        limit_time.text = String.format("%d min", limitTime)
        myAlarm.limitTime = limitTime
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_ON) {
            if (resultCode == Activity.RESULT_OK) {
                Log.e("result","ok")
                myMap.initLocation()
            } else {
                Log.e("result","cancle")
                myMap.createLocationRequest()
            }
        }
    }

    fun test(view: View) {
        Intent(this, TimeService::class.java).also { intent ->
            this.startService(intent)
        }
        Intent(this, AwakeActivity::class.java).also { intent ->
            this.startActivity(intent)
        }
    }

    fun startReportActivity(view: View) {
       Intent(this, ReportActivity::class.java).also { intent ->
            this.startActivity(intent)
        }
    }


}

class NumberPickerFragment(private val activity: MainActivity) : DialogFragment() {
    private lateinit var listener: NumberListener

    interface NumberListener {
        fun onDialogPositiveClick(dialog: DialogFragment, limitTime: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NumberListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement NumberLister"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val numberPicker = NumberPicker(activity).apply {
            value = 10
            minValue = 1
            maxValue = 60
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            scaleX = 1.5f
            scaleY = 1.5f
            scrollBarSize = 2
        }
        return activity.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Limit Time")
                .setPositiveButton("SET"
                ) { _, _ ->
                    listener.onDialogPositiveClick(this,numberPicker.value)
                }
                .setView(numberPicker)
            builder.create()
        }
    }
}