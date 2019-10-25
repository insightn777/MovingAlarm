package com.ksc.movingalarm

import android.app.NotificationManager
import android.content.*
import android.os.*
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_alarm.*
import java.lang.ref.WeakReference

class AlarmActivity : FragmentActivity(), OnMapReadyCallback {

    /*************
    Service Connect
     ***************/

    private lateinit var mService: Messenger

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mService = Messenger(binder)
            Log.e("BIND", "ON_BIND")
            val msg = Message().apply {
                what = BIND_START
                replyTo = mActivityMessenger
            }
            mService.send(msg)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.e("BIND", "UN_BIND")
        }
    }


    /*************
    Activity Control
     ***************/

    private var m = 0
    private var s = 0
    fun setCount(int: Int) {
        m = int/60
        s = int%60
        count_view.text = "${m}:${s}"
    }

    class ActivityHandler(activity: AlarmActivity) : Handler() {
        private val mActivity = WeakReference<AlarmActivity>(activity).get()
        override fun handleMessage(msg: Message) {
            mActivity?.setCount(msg.what)
        }
    }

    private val mActivityMessenger = Messenger(ActivityHandler(this))

    fun arriveDest (view: View) {
        if ( arrive ) {
            myMap.removeGeofence()
            // 서버로 도착 전송
            finish()
        }
//        val msg = Message().apply {
//            what = SERVICE_STOP
//        }
//        mService.send(msg)

        Intent(this, MyIntentService::class.java).apply {
            action = ACTION_FAIL
        }.also { intent1 ->
            startService(intent1)
        }

        finished = true
    }

    /***************
     Life Cycle
     ***************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
//        setTurnScreenOn(true)  min api 27 but now 23

        val mapFragment = supportFragmentManager.findFragmentById(R.id.alarm_map) as SupportMapFragment

        mapFragment.getMapAsync {
            onMapReady(it)
        }
        Log.e("map", "sync")
    }

    override fun onResume() {
        super.onResume()
        Intent(this, TimeService::class.java).also { intent ->
            bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(316)
    }

    override fun onPause() {
        Log.e("PAUSE","PAUSE")
        // unbindService 빼먹으면 오류남 // onBind unBind 는 한번만 호출됨 왠지는 모름 ㅎ
        if (!finished) {
            val msg = Message().apply {
                what = FORE_START
                replyTo = mActivityMessenger
            }
            mService.send(msg)
        }
        unbindService(mConnection)
        super.onPause()
    }

    /***********
     Map
    ***********/

    private val myAlarm :Alarm by lazy {
        Alarm(this)
    }
    private val myMap = Map(this)
    private var arrive = false

    override fun onMapReady(map: GoogleMap) {
        Log.e("map", "ready")
        myMap.mMap = map
        myMap.checkPermission(myAlarm.latitude, myAlarm.longitude)
        myMap.addGeofence(myAlarm.latitude, myAlarm.longitude, myAlarm.limitTime)
    }

    private var finished = false


//    val br = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            arrive = true
//        }
//    }
//    val br: BroadcastReceiver = MyBroadcastReceiver()
//
//    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
//        addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
//    }
//    registerReceiver(br, filter)
//
//    class MyBroadcastReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            arrive = true
//        }
//    }

}