package com.ksc.movingalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Alarm(context: Context) {

    private val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    private val contexT = context
    val dayCheck = BooleanArray(7)
    private val daysOfWeek = arrayOf(
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
    private var triggerTime = 0L
    var onOFF = sharedPref.getBoolean(context.getString(R.string.onOFF_key),false)
    var latitude = sharedPref.getString(context.getString(R.string.latitude_key),"35.79867668831729").toDouble()
    var longitude = sharedPref.getString(context.getString(R.string.longitude_key),"128.49933419376612").toDouble()

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

        triggerTime = calendar.timeInMillis

        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        Log.e("save", "save time: ${df.format(calendar.timeInMillis)}")

        with (sharedPref.edit()) {
            putInt(contexT.getString(R.string.hour_key), hour)
            putInt(contexT.getString(R.string.minute_key), minute)
            for (i in 0..6) {
                putBoolean(daysOfWeek[i], dayCheck[i])
            }
            putString(contexT.getString(R.string.latitude_key),latitude.toString())
            putString(contexT.getString(R.string.longitude_key),longitude.toString())
            putBoolean(contexT.getString(R.string.onOFF_key),onOFF)
            commit()
        }

        Log.e("save", "save time: ${df.format(calendar.timeInMillis)}")


        if (onOFF) {
            setAlarm()
        } else {
            alarmMgr.cancel(alarmIntent)

            // REBOOT DISABLE
            val receiver = ComponentName(contexT, BootReceiver::class.java)
            contexT.packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

    } // save()

    private val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun setAlarm () {

        alarmMgr.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
//            AlarmManager.INTERVAL_DAY,
            60000,
            alarmIntent
        )

        // REBOOT ENABLE
        val receiver = ComponentName(contexT, BootReceiver::class.java)
        contexT.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

    } // setAlarm()

}

//    alarmMgr.setAlarmClock(
//        AlarmManager.AlarmClockInfo(triggerTime, alarmIntent),
//        alarmIntent
//    )
//    fun showAlarm() :Long {
//        return alarmMgr.nextAlarmClock.triggerTime
//    }
//    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//    Log.e("setAlarm", "Alarm time: ${df.format(showAlarm())}\n")


/********************************

  Receiver Class

 ********************************/

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
       Log.e("receive", "Alarm time: ")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MovingAlarm:WAKELOCK"
        )
        wakeLock.acquire(60000)

       if ( sharedPref.getBoolean(daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1],false) ) runAlarm(context)

    }

    private fun runAlarm (context: Context) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            // putExtra(EXTRA_MESSAGE, //and something )
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
