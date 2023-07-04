package com.rib.progressiverecords

import android.content.Context
import androidx.room.Room
import androidx.room.Update
import com.rib.progressiverecords.database.RecordDatabase
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.flow.Flow
import java.util.*


class RecordRepository private constructor(context: Context) {

    private val database: RecordDatabase = Room.databaseBuilder(
        context.applicationContext,
        RecordDatabase::class.java,
        "RecordDatabase"
    ).build()

    suspend fun addSession(session: Session) = database.recordDao().addSession(session)

    suspend fun deleteSession(session: Session) = database.recordDao().deleteSession(session)

    suspend fun updateSession(session: Session) = database.recordDao().updateSession(session)

    fun getSessions(): Flow<List<SessionWithRecords>> = database.recordDao().getSessions()

    suspend fun getSession(id: UUID): Session = database.recordDao().getSession(id)

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