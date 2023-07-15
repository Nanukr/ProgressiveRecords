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
    @Insert
    suspend fun addSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    @Transaction
    @Query("SELECT * FROM session ORDER BY date ASC")
    fun getSessions(): Flow<List<SessionWithRecords>>

    @Transaction
    @Query("SELECT * FROM exercise ORDER BY exerciseName ASC")
    fun getExercises(): Flow<List<Exercise>>

    @Transaction
    @Query("SELECT * FROM session WHERE id = (:id)")
    suspend fun getSession(id: UUID): Session

    @Insert
    suspend fun addRecord(record: Record)

    @Query("SELECT * FROM record WHERE id = (:id)")
    suspend fun getRecord(id: UUID): Record

    @Insert
    suspend fun addExercise(exercise: Exercise)
}