package com.rib.progressiverecords.database

import androidx.room.*
import com.rib.progressiverecords.model.Record
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert
    suspend fun addRecord(record: Record)

    @Update
    suspend fun updateRecord(record: Record)

    @Delete
    suspend fun deleteRecord(record: Record)

    @Query("SELECT * FROM record")
    fun getRecords(): Flow<List<Record>>
}