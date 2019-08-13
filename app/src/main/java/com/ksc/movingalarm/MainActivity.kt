package com.ksc.movingalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

// const val EXTRA_MESSAGE = "main.open.setting"

/*
    TO DO :
        * GPS set
        * Alarm set
            1) Alarm on/off
            2) vibrator
        * Alarm off
        * DATE set
*/

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val myAlarm = Alarm(this)

        findViewById<TextView>(R.id.main_time).text = "${myAlarm.hour} " + if (myAlarm.minute < 10 ) ": 0${myAlarm.minute}" else ": ${myAlarm.minute}"

        switch1.isChecked = myAlarm.onOFF

        var setDay = ""
        for (i in 0..6) {
            if (myAlarm.dayCheck[i]) setDay += myAlarm.daysOfWeek[i] + "  "
        }
        findViewById<TextView>(R.id.days_of_week).text = setDay

        switch1.setOnCheckedChangeListener { _, isChecked ->
            myAlarm.onOFF = isChecked
            val mAM = myAlarmManager(this)
            if (myAlarm.onOFF) {
                mAM.setAlarm(this, myAlarm.hour, myAlarm.minute)
            } else {
                mAM.cancelAlarm()
            }
            myAlarm.save()
        }
    }

    fun openSettingActivity(view: View) {
        val intent = Intent(this, SettingActivity::class.java).apply {
            // putExtra(EXTRA_MESSAGE, //and somting )
        }
        startActivity(intent)
    }

}
