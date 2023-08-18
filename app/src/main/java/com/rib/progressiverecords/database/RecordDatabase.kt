package com.rib.progressiverecords.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session

@Database(
    entities = [Session::class, Record::class, Exercise::class],
    version = 2
)
@TypeConverters(RecordTypeConverters::class)
abstract class RecordDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}

val migration_1_2 = object: Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS UpdatedRecord (" +
                    "id BLOB NOT NULL PRIMARY KEY," +
                    "sessionId BLOB NOT NULL," +
                    "exerciseName TEXT NOT NULL," +
                    "sessionPosition INTEGER NOT NULL, " +
                    "setNumber INTEGER NOT NULL, " +
                    "repetitions INTEGER, " +
                    "weight REAL, " +
                    "exerciseDuration INTEGER" +
                    ")"
        )

        database.execSQL(
            "INSERT INTO UpdatedRecord (" +
                    "id, sessionId, exerciseName, sessionPosition, setNumber, repetitions, weight" +
                    ") " +
                    "SELECT " +
                    "id, sessionId, exerciseName, 0, setNumber, repetitions, CAST(weight AS REAL)" +
                    " FROM Record"
        )

        database.execSQL("DROP TABLE Record")

        database.execSQL("ALTER TABLE UpdatedRecord RENAME TO Record")

        database.execSQL(
            "ALTER TABLE Exercise ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 1"
        )

        database.execSQL(
            "ALTER TABLE Exercise ADD COLUMN primMuscle TEXT NOT NULL DEFAULT ''"
        )

        database.execSQL(
            "ALTER TABLE Exercise ADD COLUMN category TEXT NOT NULL DEFAULT ''"
        )

        database.execSQL(
            "ALTER TABLE Exercise ADD COLUMN isAssisted INTEGER NOT NULL DEFAULT 0"
        )

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS Muscle(muscleName TEXT NOT NULL PRIMARY KEY)"
        )

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS Category(categoryName TEXT NOT NULL PRIMARY KEY)"
        )

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS ExerciseSecMuscleCrossRef (" +
                    "exerciseName TEXT NOT NULL," +
                    "muscleName TEXT NOT NULL, " +
                    "PRIMARY KEY(exerciseName, muscleName)" +
                    ")"
        )
    }
}