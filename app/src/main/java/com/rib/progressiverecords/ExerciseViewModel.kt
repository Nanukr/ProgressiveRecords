package com.rib.progressiverecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.model.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()

    private val _exercises: MutableStateFlow<List<Exercise>> = MutableStateFlow(emptyList())
    val exercises: StateFlow<List<Exercise>>
        get() = _exercises.asStateFlow()

    init {
        viewModelScope.launch {

            recordRepository.getExercises().collect {
                _exercises.value = it
            }
        }
    }

    suspend fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            recordRepository.addExercise(exercise)
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            recordRepository.deleteExercise(exercise)
        }
    }
}