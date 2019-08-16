package com.ksc.movingalarm

import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
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

class MainActivity : AppCompatActivity() {

    private val daysID :Array<Int> by lazy {
        arrayOf(R.id.sun,R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri, R.id.sat)
    }

    val myAlarm :Alarm by lazy {
        Alarm(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
            object: TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(timePicker: TimePicker, hourOfDay:Int, minutes:Int) {
                    myAlarm.hour = hourOfDay
                    myAlarm.minute = minutes
                    set_time.text = "${myAlarm.hour} " + if (myAlarm.minute < 10 ) ": 0${myAlarm.minute}" else ": ${myAlarm.minute}"
                }
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
