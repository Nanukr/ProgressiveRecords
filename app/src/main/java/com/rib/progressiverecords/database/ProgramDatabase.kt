package com.rib.progressiverecords.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Program
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session

@Database(
    entities = [Program::class, Session::class, Record::class, Exercise::class],
    version = 1
)
abstract class ProgramDatabase : RoomDatabase() {
    abstract fun programDao(): ProgramDao

    abstract fun sessionDao(): SessionDao

    abstract fun recordDao(): RecordDao

    abstract fun exerciseDao(): ExerciseDao
}