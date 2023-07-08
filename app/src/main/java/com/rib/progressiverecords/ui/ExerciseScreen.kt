package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.ExerciseListViewModel
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.ExerciseWithRecords

@Composable
fun ExerciseScreen() {
    Scaffold(
        topBar = {
            TopBar(
                onClick = { /*TODO*/ }
            )
        }
    ) { it
        ExerciseList()
    }
}

@Composable
private fun ExerciseList(
    modifier: Modifier = Modifier,
    viewModel: ExerciseListViewModel = viewModel()
) {
    val exercises = viewModel.exercises.collectAsState(initial = emptyList())

    if (exercises.value.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No exercises registered")
        }
    } else {
        LazyColumn(
            modifier = modifier
        ) {
            items(exercises.value) {exercise ->
                ExerciseItem(exercise)
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    exercise: ExerciseWithRecords
) {
    Text(text = exercise.exercise.exerciseName, style = MaterialTheme.typography.h4)
}