package com.ksc.movingalarm.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecordDao {
    @Query("SELECT * FROM record_table ORDER BY id DESC")
    fun getAll(): LiveData<List<Record>>

    @Query("SELECT * FROM record_table WHERE id IN (:index1)")
    fun loadAllByIndex(index1: IntArray): List<Record>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: Record)

    @Query("DELETE from record_table")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(record: Record)
}