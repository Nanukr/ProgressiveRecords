package com.rib.progressiverecords.ui.exercise

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rib.progressiverecords.viewModel.ExerciseViewModel
import com.rib.progressiverecords.R
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.ExerciseSecMuscleCrossRef
import com.rib.progressiverecords.model.relations.ExerciseWithSecMuscle
import com.rib.progressiverecords.ui.TopBar
import com.rib.progressiverecords.ui.theme.ProgressiveRecordsTheme
import com.rib.progressiverecords.ui.theme.SearchBar
import com.rib.progressiverecords.ui.theme.StandardButton
import com.rib.progressiverecords.ui.theme.StandardTwoButtonsDialog
import kotlinx.coroutines.launch

@Composable
fun ExerciseListScreen(
    viewModel: ExerciseViewModel = viewModel(),
    isBeingSelected: Boolean,
    isSwapping: Boolean,
    onExercisesSelected: (List<Exercise>) -> Unit
) {
    var exerciseBeingModified by rememberSaveable { mutableStateOf(false) }
    var exerciseBeingDeleted by rememberSaveable { mutableStateOf(false) }
    var choosingFilters by rememberSaveable { mutableStateOf(false) }

    val numberOfExercisesSelected = viewModel.exercisesSelected.collectAsState().value.size

    Scaffold(
        topBar = {
            TopBar(
                onClick = {
                    exerciseBeingModified = true
                    viewModel.exerciseBeingModified = createEmptyExercise()
                },
                title = stringResource(R.string.exercise_nav_item_text),
                icon = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.create_exercise_icon_description)
            )
        }
    ) { it
        Column {
            ExerciseListHeader(
                viewModel = viewModel,
                onChooseFilters = { choosingFilters = true }
            )

            ExerciseList(
                viewModel = viewModel,
                onSelectItem = { viewModel.onChangeSelectedExercises(it, isSwapping)},
                onEditItem = { exerciseModified ->
                    exerciseBeingModified = true
                    viewModel.exerciseBeingModified = exerciseModified
                },
                onDelete = { exerciseModified ->
                    exerciseBeingDeleted = true
                    viewModel.exerciseBeingModified = exerciseModified
                },
                isBeingSelected = isBeingSelected
            )
        }

        if (isBeingSelected) {
            Box {
                Column {
                    Spacer(modifier = Modifier.weight(1f))

                    SelectButtons(
                        isSwapping = isSwapping,
                        numberSelected = numberOfExercisesSelected,
                        onAddExercises = { onExercisesSelected(viewModel.exercisesSelected.value) },
                        onCancelAdding = { onExercisesSelected(emptyList()) }
                    )
                }
            }
        }

        if (exerciseBeingModified) {
            ExerciseCreationDialog(
                exercise = viewModel.exerciseBeingModified ?: createEmptyExercise(),
                addExercise = { newExercise ->
                    exerciseBeingModified = false
                    deleteExercise(viewModel.exerciseBeingModified!!, viewModel)
                    viewModel.exerciseBeingModified = null
                    addExerciseToDb(newExercise, viewModel)
                },
                onDismissRequest = { exerciseBeingModified = false }
            )
        }

        if (exerciseBeingDeleted) {
            DeleteExerciseDialog(
                exercise = viewModel.exerciseBeingModified ?: createEmptyExercise(),
                onDismissRequest = {
                    exerciseBeingDeleted = false
                    viewModel.exerciseBeingModified = null
                                   },
                onDeleteExercise = { deletedExercise ->
                    exerciseBeingDeleted = false
                    viewModel.exerciseBeingModified = null
                    deleteExercise(deletedExercise, viewModel)
                }
            )
        }

        if (choosingFilters) {
            ExerciseListFilterDialog(
                viewModel = viewModel,
                currentFilters = viewModel.sortParams.collectAsState().value,
                onDismissRequest = { choosingFilters = false }
            )
        }
    }
    BackHandler {
        if (isBeingSelected) {
            onExercisesSelected(emptyList())
        }
    }
}

@Composable
fun ExerciseList(
    viewModel: ExerciseViewModel,
    onSelectItem: (Exercise) -> Unit,
    onEditItem: (ExerciseWithSecMuscle) -> Unit,
    onDelete: (ExerciseWithSecMuscle) -> Unit,
    isBeingSelected: Boolean
) {
    val exercises = viewModel.exercises.collectAsState(initial = emptyList())
    var exercisesSelected: List<Exercise> = emptyList()
    if (isBeingSelected) {
        exercisesSelected = viewModel.exercisesSelected.collectAsState(initial = emptyList()).value
    }

    LazyColumn(
        modifier = Modifier
            .background(color = MaterialTheme.colors.background)
    ) {
        items(exercises.value) {exercise ->
            ExerciseItem(
                exercise = exercise,
                isBeingSelected = isBeingSelected,
                isSelected = exercisesSelected.contains(exercise.exercise),
                onSelect = { onSelectItem(it) },
                onEdit = { onEditItem(it) },
                onDelete = { onDelete(it) },
            )
        }

        if (exercises.value.isEmpty()) {
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    text = stringResource(R.string.empty_exercise_list_message),
                    color = MaterialTheme.colors.onBackground,
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@Composable
private fun ExerciseListHeader(
    viewModel: ExerciseViewModel,
    onChooseFilters: () -> Unit
) {
    Row (
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchBar(
            modifier = Modifier.fillMaxWidth(0.87f),
            hint = stringResource(R.string.search_exercise_bar_hint),
            onSearch = { viewModel.onChangeSearchText(it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            modifier = Modifier
                .padding(8.dp)
                .clickable { onChooseFilters() },
            painter = painterResource(R.drawable.ic_filter),
            contentDescription = "",
            tint = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
private fun ExerciseItem(
    isBeingSelected: Boolean,
    isSelected: Boolean,
    exercise: ExerciseWithSecMuscle,
    onSelect: (Exercise) -> Unit,
    onEdit: (ExerciseWithSecMuscle) -> Unit,
    onDelete: (ExerciseWithSecMuscle) -> Unit,

) {
    val context = LocalContext.current

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.primary)
            .then(
                if (isBeingSelected) {
                    Modifier.clickable { onSelect(exercise.exercise) }
                } else {
                    Modifier
                }
            )
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = exercise.exercise.exerciseName,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = getMuscleFromEnglish(exercise.exercise.primMuscle, context) +
                            " - " +
                            getCategoryFromEnglish(exercise.exercise.category, context),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onBackground
                )
            }

            if (exercise.exercise.isDefault == 0) {
                IconButton(modifier = Modifier
                    .padding(vertical = 8.dp),
                    onClick = { onEdit(exercise) }
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit_button),
                        tint = MaterialTheme.colors.onBackground
                    )
                }

                IconButton(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    onClick = { onDelete(exercise) }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_button),
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            if (isSelected) {
                Icon(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    painter = painterResource(R.drawable.ic_filled_circle_check),
                    contentDescription = "",
                    tint = MaterialTheme.colors.secondary
                )
            }

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        }
        Divider()
    }
}

//Dialogs
@Composable
private fun DeleteExerciseDialog(
    exercise: ExerciseWithSecMuscle,
    onDeleteExercise: (ExerciseWithSecMuscle) -> Unit,
    onDismissRequest: () -> Unit
) {
    StandardTwoButtonsDialog(
        title = stringResource(R.string.confirm_exercise_deletion_message_top, exercise.exercise.exerciseName),
        text = stringResource(R.string.confirm_exercise_deletion_message_bottom),
        onConfirm = { onDeleteExercise(exercise) },
        onDismissRequest = { onDismissRequest() }
    )
}

//Buttons
@Composable
private fun SelectButtons(
    isSwapping: Boolean,
    numberSelected: Int,
    onAddExercises: () -> Unit,
    onCancelAdding: () -> Unit
) {
    val confirmString = if (isSwapping) {
        stringResource(R.string.change_exercise_in_session_button)
    } else {
        stringResource(R.string.add_exercises_to_session_button) + if (numberSelected != 0) " ($numberSelected)" else ""
    }

    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 56.dp)
            .fillMaxWidth()
    ) {
        Button(
            modifier = Modifier
                .weight(1f),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 4.dp
            ),
            onClick = { onCancelAdding() },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.LightGray
            )
        ) {
            Text(
                text = stringResource(R.string.cancel_button),
                color = Color.Black
            )
        }

        StandardButton(
            modifier = Modifier
                .weight(1f),
            text = confirmString,
            enabled = numberSelected != 0,
            onClick = { onAddExercises() }
        )
    }
}

@Preview
@Composable
private fun SelectButtonsPreview() {
    ProgressiveRecordsTheme {
        SelectButtons(onAddExercises = { /*TODO*/ }, isSwapping =  false, numberSelected = 1) {}
    }
}

//Functions
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
        exercise = Exercise(exerciseName = "", isDefault = 0, primMuscle =  "", category =  ""),
        muscles = emptyList()
    )
}