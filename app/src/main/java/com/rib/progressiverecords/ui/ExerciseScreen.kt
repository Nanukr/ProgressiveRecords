package com.rib.progressiverecords.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.rib.progressiverecords.model.Exercise
import kotlinx.coroutines.launch

@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel = viewModel(),
    isBeingSelected: Boolean,
    onExerciseSelected: (String) -> Unit
) {
    var exerciseBeingModified by rememberSaveable { mutableStateOf<Exercise?>(null) }
    var exerciseBeingDeleted by rememberSaveable { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopBar(
                onClick = { exerciseBeingModified = Exercise("") }
            )
        }
    ) { it
        ExerciseList(
            viewModel = viewModel,
            onSelectItem = { onExerciseSelected(it) },
            onEditItem = { exerciseBeingModified = it },
            onDelete = { exerciseBeingDeleted = it },
            isBeingSelected = isBeingSelected
        )

        if (exerciseBeingModified != null) {
            AddExerciseDialog(
                exercise = exerciseBeingModified!!,
                onDismissRequest = { exerciseBeingModified = null },
                upsertExercise = {
                    exerciseBeingModified = null
                    addExerciseToDb(it, viewModel)
                }
            )
        }

        if (exerciseBeingDeleted != null) {
            DeleteExerciseDialog(
                exercise = exerciseBeingDeleted!!,
                onDismissRequest = { exerciseBeingDeleted = null },
                onDeleteExercise = {
                    exerciseBeingDeleted = null
                    deleteExercise(it, viewModel)
                }
            )
        }
    }
}

@Composable
fun ExerciseList(
    viewModel: ExerciseViewModel,
    onSelectItem: (String) -> Unit,
    onEditItem: (Exercise) -> Unit,
    onDelete: (Exercise) -> Unit,
    isBeingSelected: Boolean
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
            modifier = Modifier.padding(16.dp)
        ) {
            items(exercises.value) {exercise ->
                ExerciseItem(
                    exercise = exercise,
                    onSelect = { onSelectItem(it) },
                    onEdit = { onEditItem(it) },
                    onDelete = { onDelete(it) },
                    isBeingSelected = isBeingSelected
                )
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    exercise: Exercise,
    onSelect: (String) -> Unit,
    onEdit: (Exercise) -> Unit,
    onDelete: (Exercise) -> Unit,
    isBeingSelected: Boolean

) {
    val rowModifier = if (isBeingSelected) {
        Modifier
            .padding(8.dp)
            .clickable { onSelect(exercise.exerciseName) }
    } else {
        Modifier.padding(8.dp)
    }

    Row (modifier = rowModifier) {
        Text(
            text = exercise.exerciseName,
            style = MaterialTheme.typography.h5,
            color = Color.Black
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { onEdit(exercise) }) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit exercise")
        }

        IconButton(onClick = { onDelete(exercise) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete exercise")
        }
    }
}

@Composable
private fun AddExerciseDialog(
    exercise: Exercise,
    upsertExercise: (Exercise) -> Unit,
    onDismissRequest: () -> Unit
) {
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = exercise.exerciseName))
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card {
            Column (
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                Text(text="Exercise name: ")

                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        exercise.exerciseName = it.annotatedString.toString()
                    }
                )

                Row (
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { onDismissRequest() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)) {
                        Text(text="Cancel", color = Color.White)
                    }

                    Button(onClick = { upsertExercise(exercise) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)) {
                        Text(text="Update exercise", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteExerciseDialog(
    exercise: Exercise,
    onDeleteExercise: (Exercise) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            Column (
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Are you sure you want to delete the following exercise: ${exercise.exerciseName} ?")

                Row (
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onDismissRequest() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                    ) {
                        Text(text = "No", color = Color.White)
                    }

                    Button(
                        onClick = { onDeleteExercise(exercise) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                    ) {
                        Text(text = "Yes", color = Color.White)
                    }
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

private fun deleteExercise(
    exercise: Exercise,
    viewModel: ExerciseViewModel
) {
    viewModel.viewModelScope.launch {
        viewModel.deleteExercise(exercise)
    }
}