package com.rib.progressiverecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.relations.ExerciseWithRecords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseListViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()

    private val _exercises: MutableStateFlow<List<ExerciseWithRecords>> = MutableStateFlow(emptyList())
    val exercises: StateFlow<List<ExerciseWithRecords>>
        get() = _exercises.asStateFlow()

    init {
        viewModelScope.launch {
            recordRepository.getExercises().collect {
                _exercises.value = it
            }
        }
    }

    suspend fun addExercise(exercise: Exercise) {
        recordRepository.addExercise(exercise)
    }
}