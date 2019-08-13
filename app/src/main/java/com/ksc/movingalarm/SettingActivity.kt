package com.ksc.movingalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.widget.TextView
import android.widget.TimePicker
import java.util.*


class SettingActivity : AppCompatActivity() {

    val myAlarm :Alarm by lazy {
        Alarm(this)
    }

    private val daysID :Array<Int> by lazy {
        arrayOf(R.id.sun,R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri, R.id.sat)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        for (i in 0..6) {
            if (myAlarm.dayCheck[i]) {
                findViewById<TextView>(daysID[i]).setTextColor(getColor(R.color.colorAccent))
            } else {
                findViewById<TextView>(daysID[i]).setTextColor(getColor(R.color.fontBlack))
            }
        }

        findViewById<TextView>(R.id.set_time).text = "${myAlarm.hour} " + if (myAlarm.minute < 10 ) ": 0${myAlarm.minute}" else ": ${myAlarm.minute}"
    }

    fun showTimePickerDialog(view: View) {
            val c = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                object:TimePickerDialog.OnTimeSetListener {
                    override fun onTimeSet(timePicker: TimePicker, hourOfDay:Int, minutes:Int) {
                        myAlarm.hour = hourOfDay
                        myAlarm.minute = minutes
                        findViewById<TextView>(R.id.set_time).text = "${myAlarm.hour} " + if (myAlarm.minute < 10 ) ": 0${myAlarm.minute}" else ": ${myAlarm.minute}"
                    }
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)
            )
            timePickerDialog.show()
    }

    fun saveSetting(view: View) {
        myAlarm.save()
        finish()
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

