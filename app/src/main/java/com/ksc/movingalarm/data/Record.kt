package com.ksc.movingalarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_table")
data class Record(
    @PrimaryKey(autoGenerate = true) val id :Int?,
    val month :Int,
    val day :Int,
    val dHour :Int,
    val dMinute :Int,
    val hour :Int,
    val minute :Int,
    val latitude :Double,
    val longitude :Double
)