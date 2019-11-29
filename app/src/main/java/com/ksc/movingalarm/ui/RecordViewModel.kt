package com.ksc.movingalarm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ksc.movingalarm.data.Record
import com.ksc.movingalarm.data.RecordDao
import com.ksc.movingalarm.data.RecordRoomDatabase
import kotlinx.coroutines.launch


class RecordRepository(private val recordDao: RecordDao) {
    val allRecords: LiveData<List<Record>> = recordDao.getAll()

    suspend fun insert(record: Record) {
        recordDao.insert(record)
    }

    suspend fun deleteAll() {
        recordDao.deleteAll()
    }
}

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordRepository
    val allrecords: LiveData<List<Record>>

    init {
        val recordDao = RecordRoomDatabase.getDatabase(
            application //, viewModelScope
        ).recordDao()
        repository = RecordRepository(recordDao)
        allrecords = repository.allRecords
    }

    fun insert(record: Record) = viewModelScope.launch {
        repository.insert(record)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}