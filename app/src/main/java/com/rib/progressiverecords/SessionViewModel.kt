package com.rib.progressiverecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.model.Exercise
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

    private val _exercises: MutableStateFlow<List<Exercise>> = MutableStateFlow(emptyList())
    val exercises: StateFlow<List<Exercise>>
        get() = _exercises.asStateFlow()

    init {
        viewModelScope.launch {
            recordRepository.getSessions().collect {
                _sessions.value = it
            }

            recordRepository.getExercises().collect {
                _exercises.value = it
            }
        }
    }

    suspend fun addSession(session: Session) {
        recordRepository.addSession(session)
    }

    suspend fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            recordRepository.addExercise(exercise)
        }
    }

    suspend fun getSession(id: UUID) {
        recordRepository.getSession(id)
    }
}