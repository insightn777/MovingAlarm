package com.ksc.movingalarm

import android.app.IntentService
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log

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

                handleActionFAIL()
            }
            ACTION_SUCCESS -> {
//                handleActionSUCCESS(param1, param2)
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

    private fun handleActionFAIL() {

    }

    private fun handleActionSUCCESS(param1: String, param2: String) {

    }

    override fun onDestroy() {
        unbindService(mConnection)
    }
}