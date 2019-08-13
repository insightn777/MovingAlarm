package com.ksc.movingalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.util.*

class myAlarmManager(context: Context) {

    private val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
        PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    fun setAlarm ( context: Context, hour :Int, minute :Int ) {
        // Set the alarm to start at 8:30 a.m.
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }


        val triggerTime = if (calendar.timeInMillis > System.currentTimeMillis()) {
            calendar.timeInMillis
        } else {
            calendar.timeInMillis + 1000 * 60 * 60 * 24;
        }

        alarmMgr.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            1000 * 60 * 60 * 24,
            alarmIntent
        )

        val receiver = ComponentName(context, SampleBootReceiver::class.java)

        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun cancelAlarm () {
        // If the alarm has been set, cancel it.
        alarmMgr.cancel(alarmIntent)
    }
}

class SampleBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Set the alarm here.
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val calendar = Calendar.getInstance()
        val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val daysOfWeek = arrayOf(
            context.getString(R.string.sunday),
            context.getString(R.string.monday),
            context.getString(R.string.tuesday),
            context.getString(R.string.wednesday),
            context.getString(R.string.thursday),
            context.getString(R.string.friday),
            context.getString(R.string.saturday)
        )

        if ( sharedPref.getBoolean(daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1],true)) {
            runAlarm(context)
        } else {

        }

    }

    private fun runAlarm (context: Context) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            // putExtra(EXTRA_MESSAGE, //and somting )
        }
        context.startActivity(intent)
    }
}
