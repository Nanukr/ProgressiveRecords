package com.rib.progressiverecords.database

import androidx.room.*
import com.rib.progressiverecords.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert
    suspend fun addExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("SELECT * FROM exercise")
    fun getExercises(): Flow<List<Exercise>>
}