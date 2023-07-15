package com.rib.progressiverecords.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.ExerciseViewModel
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

private const val TAG = "ExerciseList"
@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel = viewModel()
) {
    var addingExercise by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                onClick = { addingExercise = true }
            )
        }
    ) { it
        ExerciseList(viewModel = viewModel)

        if (addingExercise) {
            AddExerciseDialog(
                onDismissRequest = { addingExercise = false },
                addExercise = {
                    addingExercise = false
                    addExerciseToDb(it, viewModel)
                }
            )
        }
    }
}

@Composable
private fun ExerciseList(
    viewModel: ExerciseViewModel
) {
    val exercises = viewModel.exercises.collectAsState(initial = emptyList())

    if (exercises.value.isEmpty()) {
        Log.d(TAG, "Empty list")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No exercises registered")
        }
    } else {
        Log.d(TAG, "List not empty")
        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            items(exercises.value) {exercise ->
                ExerciseItem(exercise)
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    exercise: Exercise
) {
    Text(text = exercise.exerciseName, style = MaterialTheme.typography.h5)
}

@Composable
private fun AddExerciseDialog(
    addExercise: (Exercise) -> Unit,
    onDismissRequest: () -> Unit
) {
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card {
            Column {
                Text(text="Exercise name: ")

                TextField(
                    value = text,
                    onValueChange = { text = it }
                )

                TextButton(onClick = {
                    addExercise(Exercise(exerciseName = text.annotatedString.toString())) }) {
                    Text(text="Add exercise")
                }

                TextButton(onClick = { onDismissRequest() }) {
                    Text(text="Cancel", color = Color.Red)
                }
            }
        }
    }
}

private fun addExerciseToDb(
    exercise: Exercise,
    viewModel: ExerciseViewModel
) {
    viewModel.viewModelScope.launch {
        viewModel.addExercise(exercise)
    }
}