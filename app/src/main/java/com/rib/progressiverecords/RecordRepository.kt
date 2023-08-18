package com.rib.progressiverecords

import android.content.Context
import androidx.room.Room
import com.rib.progressiverecords.database.RecordDatabase
import com.rib.progressiverecords.database.migration_1_2
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.ExerciseWithRecords
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
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
    .addMigrations(migration_1_2)
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

    fun getSessions(): Flow<List<SessionWithRecords>> = database.recordDao().getSessions()

    fun getExercises(): Flow<List<Exercise>> = database.recordDao().getExercises()

    fun getCategoryWithExerciseName(exerciseName: String): String = database.recordDao().getCategoryWithExerciseName(exerciseName)

    suspend fun upsertExercise(exercise: Exercise) {
        coroutineScope.launch{
            database.recordDao().upsertExercise(exercise)
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