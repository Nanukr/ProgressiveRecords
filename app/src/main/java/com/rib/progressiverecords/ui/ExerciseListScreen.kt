package com.rib.progressiverecords.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.ExerciseViewModel
import com.rib.progressiverecords.R
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.ExerciseSecMuscleCrossRef
import com.rib.progressiverecords.model.relations.ExerciseWithSecMuscle
import kotlinx.coroutines.launch

@Composable
fun ExerciseListScreen(
    viewModel: ExerciseViewModel = viewModel(),
    isBeingSelected: Boolean,
    onExerciseSelected: (String) -> Unit
) {
    var exerciseBeingModified by rememberSaveable { mutableStateOf(false) }
    var exerciseBeingDeleted by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                onClick = {
                    exerciseBeingModified = true
                    viewModel.exerciseBeingModified = createEmptyExercise()
                },
                icon = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.create_exercise_icon_description)
            )
        }
    ) { it
        ExerciseList(
            viewModel = viewModel,
            onSelectItem = { onExerciseSelected(it) },
            onEditItem = {
                exerciseBeingModified = true
                viewModel.exerciseBeingModified = it
                         },
            onDelete = {
                exerciseBeingDeleted = true
                viewModel.exerciseBeingModified = it
                       },
            isBeingSelected = isBeingSelected
        )

        if (exerciseBeingModified) {
            ExerciseCreationDialog(
                exercise = viewModel.exerciseBeingModified ?: createEmptyExercise(),
                addExercise = {
                    exerciseBeingModified = false
                    deleteExercise(viewModel.exerciseBeingModified!!, viewModel)
                    viewModel.exerciseBeingModified = null
                    addExerciseToDb(it, viewModel)
                },
                onDismissRequest = { exerciseBeingModified = false },
                isBeingEdited = false
            )
        }

        if (exerciseBeingDeleted) {
            DeleteExerciseDialog(
                exercise = viewModel.exerciseBeingModified ?: createEmptyExercise(),
                onDismissRequest = {
                    exerciseBeingDeleted = false
                    viewModel.exerciseBeingModified = null
                                   },
                onDeleteExercise = {
                    exerciseBeingDeleted = false
                    viewModel.exerciseBeingModified = null
                    deleteExercise(it, viewModel)
                }
            )
        }
    }
    BackHandler {}
}

@Composable
fun ExerciseList(
    viewModel: ExerciseViewModel,
    onSelectItem: (String) -> Unit,
    onEditItem: (ExerciseWithSecMuscle) -> Unit,
    onDelete: (ExerciseWithSecMuscle) -> Unit,
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

            item {
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    exercise: ExerciseWithSecMuscle,
    onSelect: (String) -> Unit,
    onEdit: (ExerciseWithSecMuscle) -> Unit,
    onDelete: (ExerciseWithSecMuscle) -> Unit,
    isBeingSelected: Boolean

) {
    Row (
        modifier = Modifier
            .padding(8.dp)
            .then(
                if (isBeingSelected) {
                    Modifier.clickable { onSelect(exercise.exercise.exerciseName) }
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = exercise.exercise.exerciseName,
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
private fun DeleteExerciseDialog(
    exercise: ExerciseWithSecMuscle,
    onDeleteExercise: (ExerciseWithSecMuscle) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card (
            backgroundColor = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(16.dp)
                ) {
            Column (
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.confirm_exercise_deletion_message, exercise.exercise.exerciseName),
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
    exercise: ExerciseWithSecMuscle,
    viewModel: ExerciseViewModel
) {
    viewModel.viewModelScope.launch {
        viewModel.addExercise(exercise.exercise)

        exercise.muscles.forEach { muscle ->
            viewModel.addExerciseSecMuscleCrossRef(ExerciseSecMuscleCrossRef(
                exerciseName = exercise.exercise.exerciseName,
                muscleName = muscle.muscleName)
            )
        }
    }
}

private fun deleteExercise(
    exercise: ExerciseWithSecMuscle,
    viewModel: ExerciseViewModel
) {
    viewModel.viewModelScope.launch {
        viewModel.deleteExercise(exercise.exercise)

        viewModel.deleteExerciseSecMuscles(exercise.exercise.exerciseName)
    }
}

fun createEmptyExercise(): ExerciseWithSecMuscle {
    return ExerciseWithSecMuscle(
        exercise = Exercise("", 0, "", "", 0),
        muscles = emptyList()
    )
}