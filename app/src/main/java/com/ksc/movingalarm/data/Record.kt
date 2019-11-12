package com.ksc.movingalarm.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "record_table")
data class Record(
    @PrimaryKey(autoGenerate = true) val id :Int?,
    val month :Int,
    val day :Int,
    val dHour :Int,
    val dMinute :Int,
    val hour :Int,
    val minute :Int
//    @ColumnInfo(name = "latLng")val latLng :LatLng
)