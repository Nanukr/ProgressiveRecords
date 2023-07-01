package com.rib.progressiverecords.database

import androidx.room.*
import com.rib.progressiverecords.model.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun addSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("SELECT * FROM session")
    fun getSessions(): Flow<List<Session>>
}