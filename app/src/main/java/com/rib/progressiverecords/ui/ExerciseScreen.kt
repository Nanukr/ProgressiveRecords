package com.rib.progressiverecords.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Exercise
import kotlinx.coroutines.CoroutineScope

@Composable
fun ExerciseScreen() {
    var addingExercise by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                onClick = { /*TODO*/ }
            )
        }
    ) { it
        ExerciseList(addingExercise = addingExercise, onDismissDialog = { addingExercise = false })
    }
}

@Composable
private fun ExerciseList(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = viewModel(),
    addingExercise: Boolean,
    onDismissDialog: () -> Unit
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

    if (addingExercise) {
        AddExerciseDialog(
            onDismissRequest = { onDismissDialog() }
            }
        )
    }
}

@Composable
private fun ExerciseItem(
    exercise: Exercise
) {
    Text(text = exercise.exerciseName, style = MaterialTheme.typography.h4)
}

@Composable
private fun AddExerciseDialog(
    addExercise: (String) -> Unit,
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

                TextButton(onClick = { addExercise(text.annotatedString.toString()) }) {
                    Text(text="Add exercise")
                }

                TextButton(onClick = { onDismissRequest() }) {
                    Text(text="Cancel", color = Color.Red)
                }
            }
        }
    }
}