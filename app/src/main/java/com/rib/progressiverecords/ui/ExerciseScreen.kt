package com.rib.progressiverecords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.ExerciseViewModel
import com.rib.progressiverecords.R
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
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_exercise_list_message),
                color = MaterialTheme.colors.onBackground
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .background(color = MaterialTheme.colors.background)
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
            color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { onEdit(exercise) }) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit_exercise_icon_description),
                tint = MaterialTheme.colors.onBackground
            )
        }

        IconButton(onClick = { onDelete(exercise) }) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_exercise_icon_description),
                tint = MaterialTheme.colors.onBackground
            )
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
        Card (
            modifier = Modifier.background(color = MaterialTheme.colors.primaryVariant)
                ) {
            Column (
                modifier = Modifier
                    .padding(8.dp)
                    ) {
                Text (
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.upsert_exercise_name_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                TextField (
                    modifier = Modifier.padding(8.dp),
                    value = text,
                    onValueChange = {
                        text = it
                        exercise.exerciseName = it.annotatedString.toString()
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        textColor = MaterialTheme.colors.onPrimary,
                        cursorColor = MaterialTheme.colors.onPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                Row (
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton (onClick = { onDismissRequest() }) {
                        Text (
                            text = stringResource(R.string.cancel_button),
                            color = MaterialTheme.colors.secondary
                        )
                    }

                    TextButton (onClick = { upsertExercise(exercise) }) {
                        Text (
                            text = stringResource(R.string.confirm_button),
                            color = MaterialTheme.colors.secondary
                        )
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
        Card (
            modifier = Modifier.background(color = MaterialTheme.colors.primaryVariant)
                ) {
            Column (
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.confirm_exercise_deletion_message, exercise.exerciseName),
                    color = MaterialTheme.colors.onPrimary,
                    textAlign = TextAlign.Center
                )

                Row (
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton (onClick = { onDismissRequest() }) {
                        Text (
                            text = stringResource(R.string.cancel_button),
                            color = MaterialTheme.colors.secondary
                        )
                    }

                    TextButton (onClick = { onDeleteExercise(exercise) }) {
                        Text (
                            text = stringResource(R.string.confirm_button),
                            color = MaterialTheme.colors.secondary
                        )
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