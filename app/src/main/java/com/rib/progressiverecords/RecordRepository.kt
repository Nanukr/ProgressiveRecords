package com.rib.progressiverecords

import android.content.Context
import androidx.room.Room
import com.rib.progressiverecords.database.RecordDatabase
import com.rib.progressiverecords.database.migration_1_2
import com.rib.progressiverecords.database.migration_3_4
import com.rib.progressiverecords.database.migration_4_5
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.ExerciseSecMuscleCrossRef
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.ExerciseWithSecMuscle
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

private const val DATABASE_NAME = "RecordDatabase"

class RecordRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
) {

    private val database: RecordDatabase = Room.databaseBuilder(
        context.applicationContext,
        RecordDatabase::class.java,
        DATABASE_NAME
    )
    .addMigrations(migration_1_2, migration_3_4, migration_4_5)
    .createFromAsset("$DATABASE_NAME.db")
    .build()

    suspend fun addSession(session: Session) {
        coroutineScope.launch{
            database.recordDao().addSession(session)
        }
    }
    suspend fun addRecord(record: Record) {
        coroutineScope.launch{
            database.recordDao().addRecord(record)
        }
    }

    suspend fun addExercise(exercise: Exercise) {
        coroutineScope.launch{
            database.recordDao().addExercise(exercise)
        }
    }

    suspend fun addExerciseSecMuscleCrossRef(crossRef: ExerciseSecMuscleCrossRef) {
        coroutineScope.launch{
            database.recordDao().addExerciseSecMuscleCrossRef(crossRef)
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        coroutineScope.launch{
            database.recordDao().deleteExercise(exercise)
        }
    }

    suspend fun deleteSession(session: Session) {
        coroutineScope.launch{
            database.recordDao().deleteSession(session)
        }
    }

    suspend fun deleteRecordsInSession(sessionId: UUID) {
        coroutineScope.launch{
            database.recordDao().deleteRecordsInSession(sessionId)
        }
    }

    suspend fun deleteExerciseSecMuscles(exerciseName: String) {
        coroutineScope.launch {
            database.recordDao().deleteExerciseSecMuscles(exerciseName)
        }
    }

    fun getSessions(): Flow<List<SessionWithRecords>> = database.recordDao().getSessions()

    fun getExercises(): Flow<List<ExerciseWithSecMuscle>> = database.recordDao().getExercises()

    companion object {
        private var INSTANCE: RecordRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = RecordRepository(context)
            }
        }

        fun get(): RecordRepository {
            return INSTANCE ?: throw IllegalStateException("RecordsRepository must be initialized")
        }
    }
}