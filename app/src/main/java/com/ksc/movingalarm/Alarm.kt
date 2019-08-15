package com.ksc.movingalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Alarm(context: Context) {

    private val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    private val contexT = context
    val dayCheck = BooleanArray(7)
    val daysOfWeek = arrayOf(
        context.getString(R.string.sunday),
        context.getString(R.string.monday),
        context.getString(R.string.tuesday),
        context.getString(R.string.wednesday),
        context.getString(R.string.thursday),
        context.getString(R.string.friday),
        context.getString(R.string.saturday)
    )
    var hour = sharedPref.getInt(context.getString(R.string.hour_key),0)
    var minute = sharedPref.getInt(context.getString(R.string.minute_key),0)
    private var triggerTime = sharedPref.getLong(context.getString(R.string.trigger_time_key),0)
    var onOFF = sharedPref.getBoolean(context.getString(R.string.onOFF_key),false)

    init {
        for (i in 0..6) {
            dayCheck[i] = sharedPref.getBoolean(daysOfWeek[i],false)
        }
    }

    fun save () {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if ( ( System.currentTimeMillis() + 10000 ) - calendar.timeInMillis  > 0) {
//            triggerTime = calendar.timeInMillis + 86400000L
//            calendar.add(Calendar.DATE,2)
            Log.e("next day", "gogo")
        } else {
//            triggerTime = calendar.timeInMillis
        }

        triggerTime = calendar.timeInMillis

//        Log.e("save","Trigger time: ${triggerTime} \n calendar time : ${calendar.timeInMillis} \n current Time: ${System.currentTimeMillis()}")

        with (sharedPref.edit()) {
            putInt(contexT.getString(R.string.hour_key), hour)
            putInt(contexT.getString(R.string.minute_key), minute)
            putLong(contexT.getString(R.string.trigger_time_key),triggerTime)
            for (i in 0..6) {
                putBoolean(daysOfWeek[i], dayCheck[i])
            }
            putBoolean(contexT.getString(R.string.onOFF_key),onOFF)
            commit()
        }

        if (onOFF) {
            setAlarm()
        } else {
            cancelAlarm()
        }
    } // save()

    private val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun setAlarm () {

        alarmMgr.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, alarmIntent),
            alarmIntent
        )

        val receiver = ComponentName(contexT, BootReceiver::class.java)
        contexT.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        Log.e("setAlarm", "Alarm time: ${df.format(showAlarm().triggerTime)}")

    } // setAlarm()

    fun repeatAlarm() {
        triggerTime += 2*86400000L
        with (sharedPref.edit()) {
            putLong(contexT.getString(R.string.trigger_time_key),triggerTime)
            commit()
        }
        setAlarm()
    }

    fun showAlarm() :AlarmManager.AlarmClockInfo {
        return alarmMgr.nextAlarmClock
    }

    private fun cancelAlarm () {
        // If the alarm has been set, cancel it.
        alarmMgr.cancel(alarmIntent)
    }

}

/********************************

  Receiver Class

 ********************************/

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val calendar = Calendar.getInstance()
        val myAlarm = Alarm(context)

        if ( myAlarm.dayCheck[calendar.get(Calendar.DAY_OF_WEEK) - 1] ) runAlarm(context)

//            myAlarm.repeatAlarm()

        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        Log.e("receive", "Alarm time: ${df.format(myAlarm.showAlarm().triggerTime)}")
    }

    private fun runAlarm (context: Context) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            // putExtra(EXTRA_MESSAGE, //and somting )
        }
        context.startActivity(intent)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Set the alarm here.
        }
    }
}
