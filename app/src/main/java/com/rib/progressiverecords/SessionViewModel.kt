package com.rib.progressiverecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _detailedSession = MutableStateFlow<SessionWithRecords?>(null)
    val detailedSession = _detailedSession.asStateFlow()

    init {
        viewModelScope.launch {
            recordRepository.getSessions().collect {
                _sessions.value = it
            }
        }
    }

    fun changeDetailedSession(session: SessionWithRecords?) {
        _detailedSession.value = session
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
}