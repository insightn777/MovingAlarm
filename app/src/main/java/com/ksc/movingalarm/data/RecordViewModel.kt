package com.ksc.movingalarm.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class RecordRepository(private val recordDao: RecordDao) {
    val allRecords: LiveData<List<Record>> = recordDao.getAll()

    suspend fun insert(record: Record) {
        recordDao.insert(record)
    }
}

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordRepository
    val allrecords: LiveData<List<Record>>

    init {
        val recordDao = RecordRoomDatabase.getDatabase(application/*, viewModelScope*/).recordDao()
        repository = RecordRepository(recordDao)
        allrecords = repository.allRecords
    }

    fun insert(record: Record) = viewModelScope.launch {
        repository.insert(record)
    }
}