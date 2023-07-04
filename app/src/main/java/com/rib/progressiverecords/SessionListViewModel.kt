package com.rib.progressiverecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class SessionListViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()

    private val _sessions: MutableStateFlow<List<SessionWithRecords>> = MutableStateFlow(emptyList())
    val sessions: StateFlow<List<SessionWithRecords>>
        get() = _sessions.asStateFlow()

    init {
        viewModelScope.launch {
            recordRepository.getSessions().collect {
                _sessions.value = it
            }
        }
    }

    suspend fun addSession(session: Session) {
        recordRepository.addSession(session)
    }
}