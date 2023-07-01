package com.rib.progressiverecords.database

import androidx.room.*
import com.rib.progressiverecords.model.Program
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Insert
    suspend fun addProgram(program: Program)

    @Update
    suspend fun updateProgram(program: Program)

    @Delete
    suspend fun deleteProgram(program: Program)

    @Query("SELECT * FROM program")
    fun getPrograms(): Flow<List<Program>>
}