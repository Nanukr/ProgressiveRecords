package com.rib.progressiverecords

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.ExerciseSecMuscleCrossRef
import com.rib.progressiverecords.model.relations.ExerciseWithSecMuscle
import com.rib.progressiverecords.model.relations.SessionWithRecords
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()

    private val _sortParams = MutableStateFlow(ExerciseSortParams(searchText = "", muscles = emptyList(), categories = emptyList()))
    val sortParams = _sortParams.asStateFlow()

    private val _exercises: MutableStateFlow<List<ExerciseWithSecMuscle>> = MutableStateFlow(emptyList())
    val exercises = _sortParams
        .combine(_exercises) { sortParams, exercises ->
            exercises.filter {
                matchesFilters(exercise = it.exercise, filters = sortParams)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _exercises.value
        )

    private val _exercisesSelected: MutableStateFlow<List<Exercise>> = MutableStateFlow(emptyList())
    val exercisesSelected = _exercisesSelected.asStateFlow()

    var exerciseBeingModified: ExerciseWithSecMuscle? = null

    init {
        viewModelScope.launch {
            recordRepository.getExercises().collect {
                _exercises.value = it
            }
        }
    }

    fun onChangeSearchText(searchText: String) {
        _sortParams.value = _sortParams.value.copy(searchText = searchText)
    }

    fun onChangeFilterMuscles(muscles: List<String>) {
        _sortParams.value = _sortParams.value.copy(muscles = muscles)
    }

    fun onChangeFilterCategories(categories: List<String>) {
        _sortParams.value = _sortParams.value.copy(categories = categories)
    }

    fun onChangeSelectedExercises(exercise: Exercise) {
        if (_exercisesSelected.value.contains(exercise)) {
            _exercisesSelected.value -= exercise
        } else {
            _exercisesSelected.value += exercise
        }
    }

    private fun matchesFilters(exercise: Exercise, filters: ExerciseSortParams): Boolean {
        val matchesSearch: Boolean = if(filters.searchText.isBlank()) {
            true
        } else {
            filters.searchText.lowercase() in exercise.exerciseName.lowercase()
        }

        val matchesMuscles: Boolean = if (filters.muscles.isEmpty()) {
            true
        } else {
            filters.muscles.contains(exercise.primMuscle)
        }

        val matchesCategories: Boolean = if (filters.categories.isEmpty()) {
            true
        } else {
            filters.categories.contains(exercise.category)
        }

        return matchesSearch && matchesMuscles && matchesCategories
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