package com.ksc.movingalarm.service

import android.app.job.JobParameters
import android.app.job.JobService
import com.ksc.movingalarm.Alarm

class RenewJobService :JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        Alarm(applicationContext).setAlarm()
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        return false
    }
}