package com.ksc.movingalarm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Record::class), version = 2)
abstract class RecordRoomDatabase :RoomDatabase() {

    abstract fun recordDao(): RecordDao

//    private class RecordDatabaseCallback(
//        private val scope: CoroutineScope
//    ) : RoomDatabase.Callback() {
//
//        override fun onOpen(db: SupportSQLiteDatabase) {
//            super.onOpen(db)
//            // Add starter
//            INSTANCE?.let { database ->
//                scope.launch {
//                    var recordDao = database.recordDao()
//                     var record = Record(
//                         null,
//                         month = 11,
//                         day = 12,
//                         dHour = 13,
//                         dMinute = 14,
//                         hour = 15,
//                         minute = 16
//                     )
//                     recordDao.insert(record)
//                }
//            }
//        }
//    }


    companion object {
        @Volatile
        private var INSTANCE :RecordRoomDatabase? = null

        fun getDatabase(
            context: Context
//            scope: CoroutineScope
        ) : RecordRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =  Room.databaseBuilder(
                    context.applicationContext,
                    RecordRoomDatabase::class.java,
                    "record_database"
                )
//                    .addCallback(RecordDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun add(
            context: Context,
            record: Record
        ) {
            getDatabase(context).recordDao().add(record)
        }

    }
}