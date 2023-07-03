package com.rib.progressiverecords.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session

@Database(
    entities = [Session::class, Record::class, Exercise::class],
    version = 1
)
@TypeConverters(RecordTypeConverters::class)
abstract class RecordDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}