package com.rib.progressiverecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()

    private val _exercises: MutableStateFlow<List<Exercise>> = MutableStateFlow(emptyList())
    val exercises: StateFlow<List<Exercise>>
        get() = _exercises.asStateFlow()

    private val _exerciseBeingModified = MutableStateFlow<Exercise?>(null)
    val exerciseBeingModified = _exerciseBeingModified.asStateFlow()

    init {
        viewModelScope.launch {

            recordRepository.getExercises().collect {
                _exercises.value = it
            }
        }
    }

    fun changeExerciseBeingModified(exercise: Exercise?) {
        _exerciseBeingModified.value = exercise
    }

    suspend fun upsertExercise(exercise: Exercise) {
        viewModelScope.launch {
            recordRepository.upsertExercise(exercise)
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            recordRepository.deleteExercise(exercise)
        }
    }
}