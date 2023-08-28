package com.rib.progressiverecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.ExerciseSecMuscleCrossRef
import com.rib.progressiverecords.model.relations.ExerciseWithSecMuscle
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()

    private val _exercises: MutableStateFlow<List<ExerciseWithSecMuscle>> = MutableStateFlow(emptyList())
    val exercises: StateFlow<List<ExerciseWithSecMuscle>>
        get() = _exercises.asStateFlow()

    var exerciseBeingModified: ExerciseWithSecMuscle? = null

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

    suspend fun addExerciseSecMuscleCrossRef(crossRef: ExerciseSecMuscleCrossRef) {
        viewModelScope.launch {
            recordRepository.addExerciseSecMuscleCrossRef(crossRef)
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            recordRepository.deleteExercise(exercise)
        }
    }

    suspend fun deleteExerciseSecMuscles(exerciseName: String) {
        viewModelScope.launch {
            recordRepository.deleteExerciseSecMuscles(exerciseName)
        }
    }
}