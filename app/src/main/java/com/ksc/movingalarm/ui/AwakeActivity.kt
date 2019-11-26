package com.ksc.movingalarm.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ksc.movingalarm.R

class AwakeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_awake)
    }

    fun awake(view: View) {
        Intent(this, AlarmActivity::class.java).also { intent ->
           startActivity(intent)
        }
        finish()
    }
}
