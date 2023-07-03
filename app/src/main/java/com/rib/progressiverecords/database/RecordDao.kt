package com.rib.progressiverecords.database

import androidx.room.*
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
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
    fun getSessions(): Flow<List<Session>>

    @Transaction
    @Query("SELECT * FROM session WHERE id=(:id)")
    suspend fun getSession(id: UUID): Session

    @Insert
    suspend fun addRecord(record: Record)

    @Insert
    suspend fun addExercise(exercise: Exercise)

    @Query("SELECT exerciseName FROM exercise WHERE id=(:id)")
    suspend fun getExerciseNameById(id: UUID): String
}