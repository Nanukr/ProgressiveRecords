package com.rib.progressiverecords.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rib.progressiverecords.model.*

@Database(
    entities = [Session::class, Record::class, Exercise::class, ExerciseSecMuscleCrossRef::class, Category::class, Muscle::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ]
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

val migration_3_4 = object: Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS UpdatedExercise (" +
                    "exerciseName TEXT NOT NULL PRIMARY KEY," +
                    "isDefault INTEGER NOT NULL DEFAULT 1," +
                    "primMuscle TEXT NOT NULL DEFAULT ''," +
                    "category TEXT NOT NULL DEFAULT ''" +
                    ")"
        )

        database.execSQL(
            "INSERT INTO UpdatedExercise (" +
                    "exerciseName, isDefault, primMuscle, category" +
                    ") " +
                    "SELECT " +
                    "exerciseName, isDefault, primMuscle, category" +
                    " FROM Exercise"
        )

        database.execSQL("DROP TABLE Exercise")

        database.execSQL("ALTER TABLE UpdatedExercise RENAME TO Exercise")

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS UpdatedRecord (" +
                    "id BLOB NOT NULL PRIMARY KEY," +
                    "sessionId BLOB NOT NULL," +
                    "exerciseName TEXT NOT NULL," +
                    "sessionPosition INTEGER NOT NULL, " +
                    "setNumber INTEGER NOT NULL, " +
                    "repetitions INTEGER, " +
                    "weight REAL, " +
                    "exerciseDuration INTEGER, " +
                    "distance REAL" +
                    ")"
        )

        database.execSQL(
            "INSERT INTO UpdatedRecord (" +
                    "id, sessionId, exerciseName, sessionPosition, setNumber, repetitions, weight" +
                    ") " +
                    "SELECT " +
                    "id, sessionId, exerciseName, sessionPosition, setNumber, repetitions, weight" +
                    " FROM Record"
        )

        database.execSQL("DROP TABLE Record")

        database.execSQL("ALTER TABLE UpdatedRecord RENAME TO Record")

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS UpdatedSession (" +
                    "id BLOB NOT NULL PRIMARY KEY," +
                    "sessionName TEXT NOT NULL," +
                    "date INTEGER NOT NULL," +
                    "isTemplate INTEGER NOT NULL DEFAULT 0" +
                    ")"
        )

        database.execSQL(
            "INSERT INTO UpdatedSession (" +
                    "id, sessionName, date" +
                    ") " +
                    "SELECT " +
                    "id, sessionName, date" +
                    " FROM Session"
        )

        database.execSQL("DROP TABLE Session")

        database.execSQL("ALTER TABLE UpdatedSession RENAME TO Session")
    }

    val migration_4_5 = object: Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS UpdatedExercise (" +
                        "id BLOB NOT NULL PRIMARY KEY" +
                        "exerciseName TEXT NOT NULL," +
                        "isDefault INTEGER NOT NULL DEFAULT 1," +
                        "primMuscle TEXT NOT NULL DEFAULT ''," +
                        "category TEXT NOT NULL DEFAULT ''" +
                        ")"
            )

            database.execSQL(
                "INSERT INTO UpdatedExercise (" +
                        "exerciseName, isDefault, primMuscle, category" +
                        ") " +
                        "SELECT " +
                        "exerciseName, isDefault, primMuscle, category" +
                        " FROM Exercise"
            )

            database.execSQL("DROP TABLE Exercise")

            database.execSQL("ALTER TABLE UpdatedExercise RENAME TO Exercise")
        }
    }
}