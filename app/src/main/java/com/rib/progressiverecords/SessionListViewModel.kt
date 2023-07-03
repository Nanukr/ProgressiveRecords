package com.rib.progressiverecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionListViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()

    private val _sessions: MutableStateFlow<List<Session>> = MutableStateFlow(emptyList())
    val sessions: StateFlow<List<Session>>
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