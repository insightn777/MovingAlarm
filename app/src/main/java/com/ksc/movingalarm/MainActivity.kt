package com.ksc.movingalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

/*
    TO DO :
        * GPS set
        * Alarm set
            1) Alarm on/off
            2) vibrator
        * Alarm off
        * DATE set
*/

// const val EXTRA_MESSAGE = "main.open.setting"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val myAlarm = Alarm(this)

        main_time.text = "${myAlarm.hour} " + if (myAlarm.minute < 10 ) ": 0${myAlarm.minute}" else ": ${myAlarm.minute}"

        var setDay = ""
        for (i in 0..6) {
            if (myAlarm.dayCheck[i]) setDay += myAlarm.daysOfWeek[i] + "  "
        }
        days_of_week.text = setDay

        on_switch.isChecked = myAlarm.onOFF

        on_switch.setOnCheckedChangeListener { _, isChecked ->
            myAlarm.onOFF = isChecked
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
