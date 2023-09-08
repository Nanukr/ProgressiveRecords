package com.rib.progressiverecords.database

import androidx.room.*
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.ExerciseSecMuscleCrossRef
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.ExerciseWithRecords
import com.rib.progressiverecords.model.relations.ExerciseWithSecMuscle
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
    suspend fun addExercise(exercise: Exercise)

    @Upsert
    suspend fun addExerciseSecMuscleCrossRef(crossRef: ExerciseSecMuscleCrossRef)

    @Delete
    suspend fun deleteSession(session: Session)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Transaction
    @Query("DELETE FROM record WHERE sessionId = :sessionId")
    suspend fun deleteRecordsInSession(sessionId: UUID)

    @Transaction
    @Query("DELETE FROM ExerciseSecMuscleCrossRef WHERE exerciseName = :exerciseName")
    suspend fun deleteExerciseSecMuscles(exerciseName: String)

    @Transaction
    @Query("SELECT * FROM session ORDER BY date ASC")
    fun getSessions(): Flow<List<SessionWithRecords>>

    @Transaction
    @Query("SELECT * FROM exercise ORDER BY exerciseName ASC")
    fun getExercises(): Flow<List<ExerciseWithSecMuscle>>
}