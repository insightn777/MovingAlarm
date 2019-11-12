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

private const val EXTRA_PARAM1 = "com.ksc.movingalarm.extra.PARAM1"
private const val EXTRA_PARAM2 = "com.ksc.movingalarm.extra.PARAM2"

class MyIntentService : IntentService("MyIntentService") {

    override fun onHandleIntent(intent: Intent) {
//        val param1 = intent.getStringExtra(EXTRA_PARAM1)
//        val param2 = intent.getStringExtra(EXTRA_PARAM2)
        Intent(this, TimeService::class.java).also { intent1 ->
            bindService(intent1, mConnection, Context.BIND_AUTO_CREATE)
        }
        when (intent.action) {
            ACTION_FAIL -> {
                handleAction(false)
            }
            ACTION_SUCCESS -> {
                handleAction(true)
            }
        }
    }

    fun sendMessege () {
        val msg = Message().apply {
            what = SERVICE_STOP
        }
        mService.send(msg)
    }

    private var mBound = false
    private lateinit var mService: Messenger

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.e("IntentService", "ON_BIND")
            mService = Messenger(binder)
            mBound = true
            sendMessege()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.e("IntentService", "UN_BIND")
            mBound = false
        }
    }

    private fun handleAction(success: Boolean) {
        val alarm = Alarm(applicationContext)
        val calendar = Calendar.getInstance()
        val newRecord = Record(
            null,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DATE),
            if (success) alarm.hour else 0,
            if (success) alarm.minute else 0,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
            )
        RecordRoomDatabase.add(this,newRecord)
    }

    override fun onDestroy() {
        unbindService(mConnection)
    }
}