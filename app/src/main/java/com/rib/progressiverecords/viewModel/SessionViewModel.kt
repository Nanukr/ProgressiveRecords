package com.rib.progressiverecords.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.RecordRepository
import com.rib.progressiverecords.SessionCreationVariation
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class SessionViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()

    private val _sessions: MutableStateFlow<List<SessionWithRecords>> = MutableStateFlow(emptyList())
    val sessions: StateFlow<List<SessionWithRecords>>
        get() = _sessions.asStateFlow()

    var detailedSession: SessionWithRecords? = null

    var createdSession: Session? = null

    var newRecords = emptyList<Record>()

    var templateRecords = emptyList<Record>()

    var templateRecordsMap = emptyMap<UUID, Record>()

    var positionBeingModified: Int? = null

    var previousSets: Int? = null

    var checkedRecords: List<Record>? = null

    var variation: SessionCreationVariation? = null

    init {
        viewModelScope.launch {
            recordRepository.getSessions().collect {
                _sessions.value = it
            }
        }
    }

    suspend fun addSession(session: Session) {
        viewModelScope.launch {
            recordRepository.addSession(session)
        }
    }

    suspend fun addRecord(record: Record) {
        viewModelScope.launch {
            recordRepository.addRecord(record)
        }
    }

    suspend fun deleteSession(session: Session) {
        viewModelScope.launch {
            recordRepository.deleteSession(session)
        }
    }

    suspend fun deleteRecordsInSession(sessionId: UUID) {
        viewModelScope.launch {
            recordRepository.deleteRecordsInSession(sessionId)
        }
    }


}