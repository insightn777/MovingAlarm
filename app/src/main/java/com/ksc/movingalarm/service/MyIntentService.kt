package com.ksc.movingalarm.service

import android.app.IntentService
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.ksc.movingalarm.Alarm
import com.ksc.movingalarm.data.Record
import com.ksc.movingalarm.data.RecordRoomDatabase
import java.util.*

const val ACTION_FAIL = "com.ksc.movingalarm.action.FAIL"
const val ACTION_SUCCESS = "com.ksc.movingalarm.action.SUCCESS"

class MyIntentService : IntentService("MyIntentService") {

    override fun onHandleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_FAIL -> {
                handleAction(false)
            }
            ACTION_SUCCESS -> {
                handleAction(true)
            }
        }
    }

    private fun handleAction(success: Boolean) {

        Intent(this, TimeService::class.java).apply {
            putExtra("success",success)
        }.also {
            stopService(it)
        }

        val alarm = Alarm(applicationContext)
        val calendar = Calendar.getInstance()
        val newRecord = Record(
            null,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DATE),
            alarm.hour,
            alarm.minute,
            if (success) calendar.get(Calendar.HOUR_OF_DAY) else 0,
            if (success) calendar.get(Calendar.MINUTE) else 0,
            alarm.latitude,
            alarm.longitude
            )
        RecordRoomDatabase.add(this,newRecord)
    }
}