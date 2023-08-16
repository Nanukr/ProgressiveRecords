package com.rib.progressiverecords.database

import androidx.room.*
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.ExerciseWithRecords
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface RecordDao {
    @Upsert
    suspend fun addSession(session: Session)

    @Upsert
    suspend fun addRecord(record: Record)

    @Upsert
    suspend fun upsertExercise(exercise: Exercise)

    @Delete
    suspend fun deleteSession(session: Session)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Transaction
    @Query("SELECT * FROM session ORDER BY date ASC")
    fun getSessions(): Flow<List<SessionWithRecords>>

    @Transaction
    @Query("SELECT * FROM exercise ORDER BY exerciseName ASC")
    fun getExercises(): Flow<List<Exercise>>

    @Transaction
    @Query("DELETE FROM record WHERE sessionId IN (SELECT id FROM session WHERE id = :sessionId)")
    suspend fun deleteRecordsInSession(sessionId: UUID)
}