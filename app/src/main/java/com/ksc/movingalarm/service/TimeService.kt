package com.ksc.movingalarm.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ksc.movingalarm.ui.AlarmActivity
import com.ksc.movingalarm.R
import java.lang.ref.WeakReference

const val BIND_START = 1
const val FORE_START = 2

class TimeService : Service() {

    var remainTime = 0
    var success = false
    var mBound = false
    private lateinit var mMessenger : Messenger
    private val mySharedPreferences by lazy {
        getSharedPreferences(applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    internal class ServiceHandler(service: TimeService) : Handler() {
        private val mService = WeakReference<TimeService>(service).get()
        override fun handleMessage(msg: Message) {
            Log.e("BUTTON","PUSH")
            when (msg.what) {
                BIND_START -> mService?.startBindService(msg.replyTo)
                FORE_START -> mService?.startForegroundService1()
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun stopService(name: Intent?): Boolean {
        success = name!!.getBooleanExtra("success",false)
        return super.stopService(name)
    }

    override fun onCreate() {
        remainTimeThread = ServiceThread()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        remainTime = mySharedPreferences.getInt(this.getString(R.string.limitTime_key),0) * 60
        mySharedPreferences.edit()
            .putBoolean("run",true)
            .apply()
        startForegroundService1()
        remainTimeThread.start()

        return START_STICKY
    }

    private lateinit var mActivityMessenger: Messenger

    fun startBindService(messenger: Messenger) {
        Log.e("start", "Bound")
        mActivityMessenger = messenger
        mBound = true
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {         // onBind 는 bind 할때마다 호출되지 않음
        mMessenger = Messenger(ServiceHandler(this))
        return mMessenger.binder
    }

    private fun startForegroundService1() {
        Log.e("start", "foreground")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "mychannel"
            val descriptionText = "mychannel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NotificationCompat.CATEGORY_ALARM, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        val notification = NotificationCompat.Builder(this, NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setContentTitle("Arrive!!!")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(true)

        startForeground(316,notification.build())
        mBound = false
    }

    override fun onDestroy() {
        mySharedPreferences.edit()
            .putBoolean("run",false)
            .apply()
        success = true
        stopForeground(true)
    }

    /***************
    Thread
     ***************/

    private lateinit var remainTimeThread: ServiceThread

    private var m = 0
    private var s = 0

    fun runFore(rTime : Int) {

        val pendingIntent: PendingIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        m = rTime/60
        s = rTime%60
        val notification = NotificationCompat.Builder(this, NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setContentTitle("${m}:${s}")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(316, notification.build().apply {
                flags = Notification.FLAG_ONGOING_EVENT
            })
        }
    }

    fun runBind(rTime: Int) {
        mActivityMessenger.send(Message().apply { what = rTime })
    }

    inner class ServiceThread : Thread() {
        override fun run() {
            super.run()

            while (remainTime-- > 0) {
                try {
                    Log.e("Thread", "$remainTime")
                    if(mBound){
                        runBind(remainTime)
                    }
                    else {
                        runFore(remainTime)
                    }
                    sleep(1000)
                } catch (e: InterruptedException) {
                    interrupt()
                    Log.e("Thread", "$e")
                }
                if (success) break
            }

            if (success) {
                // IntentService 에서 성공 처리
                Log.e("T_success", "$success")
            } else {
                // 실패 처리
                Intent(applicationContext, MyIntentService::class.java).apply {
                    action = ACTION_FAIL
                }.also {
                    startService(it)
                }
            }
        }
    }
}