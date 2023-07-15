package com.rib.progressiverecords

import android.content.Context
import androidx.room.Room
import com.rib.progressiverecords.database.RecordDatabase
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


class RecordRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
) {

    private val database: RecordDatabase = Room.databaseBuilder(
        context.applicationContext,
        RecordDatabase::class.java,
        "RecordDatabase"
    ).build()

    suspend fun addSession(session: Session) = database.recordDao().addSession(session)

    suspend fun deleteSession(session: Session) = database.recordDao().deleteSession(session)

    suspend fun updateSession(session: Session) = database.recordDao().updateSession(session)

    fun getSessions(): Flow<List<SessionWithRecords>> = database.recordDao().getSessions()

    fun getExercises(): Flow<List<Exercise>> = database.recordDao().getExercises()

    suspend fun getSession(id: UUID): Session = database.recordDao().getSession(id)

    suspend fun getRecord(id: UUID): Record = database.recordDao().getRecord(id)

    suspend fun addExercise(exercise: Exercise){
        coroutineScope.launch{
            database.recordDao().addExercise(exercise)
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