package com.ksc.movingalarm

import android.content.Context

class Alarm(context: Context) {

    val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    val contexT = context
    var hour = 0
    var minute = 0
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
    var onOFF = false

    init {
        hour = sharedPref.getInt(context.getString(R.string.hour_key),0)
        minute = sharedPref.getInt(context.getString(R.string.minute_key),0)

        for (i in 0..6) {
            dayCheck[i] = sharedPref.getBoolean(daysOfWeek[i],true)
        }

        onOFF = sharedPref.getBoolean(context.getString(R.string.onOFF_key),false)
    }

    fun save () {
        with (sharedPref.edit()) {
            putInt(contexT.getString(R.string.hour_key), hour)
            putInt(contexT.getString(R.string.minute_key), minute)
            for (i in 0..6) {
                putBoolean(daysOfWeek[i], dayCheck[i])
            }
            putBoolean(contexT.getString(R.string.onOFF_key),onOFF)
            commit()
        }
    }
}