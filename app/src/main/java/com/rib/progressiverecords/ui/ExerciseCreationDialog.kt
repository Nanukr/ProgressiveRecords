package com.rib.progressiverecords.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rib.progressiverecords.R
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.relations.ExerciseWithSecMuscle
import com.rib.progressiverecords.ui.theme.ProgressiveRecordsTheme
import com.rib.progressiverecords.ui.theme.StandardTextField

@Composable
fun ExerciseCreationDialog(
    exercise: ExerciseWithSecMuscle,
    addExercise: (ExerciseWithSecMuscle) -> Unit,
    onDismissRequest: () -> Unit,
    isBeingEdited: Boolean
) {
    var choosingPrimMuscle by rememberSaveable { mutableStateOf(false) }
    var choosingSecMuscle by rememberSaveable { mutableStateOf(false) }
    var choosingCategory by rememberSaveable { mutableStateOf(false) }
    var missingEntriesDialog by rememberSaveable { mutableStateOf(false) }

    var secMuscles = ""

    for (muscle in exercise.muscles) {
        secMuscles += if (muscle.muscleName != exercise.muscles[exercise.muscles.lastIndex].muscleName) {
            "${muscle.muscleName}, "
        } else {
            muscle.muscleName
        }
    }
    var exerciseName by rememberSaveable{ mutableStateOf(exercise.exercise.exerciseName) }

    var selectedPrimMuscle by rememberSaveable { mutableStateOf(exercise.exercise.primMuscle) }
    var selectedSecMuscles by rememberSaveable { mutableStateOf(secMuscles) }

    var selectedCategory by rememberSaveable { mutableStateOf(exercise.exercise.category) }
    var isAssisted by rememberSaveable { mutableStateOf(false) }

    val muscles = listOf(
        stringResource(R.string.muscle_name_chest),
        stringResource(R.string.muscle_name_biceps),
        stringResource(R.string.muscle_name_triceps),
        stringResource(R.string.muscle_name_forearms),
        stringResource(R.string.muscle_name_core),
        stringResource(R.string.muscle_name_traps),
        stringResource(R.string.muscle_name_lats),
        stringResource(R.string.muscle_name_shoulders),
        stringResource(R.string.muscle_name_upper_back),
        stringResource(R.string.muscle_name_lower_back),
        stringResource(R.string.muscle_name_quads),
        stringResource(R.string.muscle_name_hamstrings),
        stringResource(R.string.muscle_name_calves),
        stringResource(R.string.muscle_name_adductors),
        stringResource(R.string.muscle_name_abductors),
        stringResource(R.string.muscle_name_full_body),
        stringResource(R.string.muscle_name_olympic),
        stringResource(R.string.muscle_category_name_other),
    )

    val categories = listOf(
        stringResource(R.string.category_name_dumbbells),
        stringResource(R.string.category_name_barbell),
        stringResource(R.string.category_name_bodyweight),
        stringResource(R.string.category_name_resistance_bands),
        stringResource(R.string.category_name_kettlebells),
        stringResource(R.string.category_name_trap_bar),
        stringResource(R.string.category_name_smith_machine),
        stringResource(R.string.category_name_machine),
        stringResource(R.string.category_name_cable),
        stringResource(R.string.category_name_cardio),
        stringResource(R.string.muscle_category_name_other),
    )

    Dialog( onDismissRequest = { onDismissRequest() } ) {
        Card (
            backgroundColor = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column (
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Row (
                    modifier = Modifier
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                        ) {
                    Text (
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.create_exercise_caption),
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.h6
                    )
                }

                //Exercise name
                Text (
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.upsert_exercise_name_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                Card (
                    modifier = Modifier
                        .padding(8.dp),
                    backgroundColor = MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.secondary)
                ) {
                    StandardTextField(
                        entryValue = exerciseName,
                        onValueChange = {
                            exerciseName = it
                            exercise.exercise.exerciseName = it
                        },
                        isNumeric = false,
                        modifier = Modifier.padding(4.dp),
                        textAlign = TextAlign.Left
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                //Muscles
                Text (
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.upsert_target_muscles_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                Card (
                    modifier = Modifier
                        .padding(8.dp),
                    backgroundColor = MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.secondary)
                ) {
                    Column {
                        //Primary muscle
                        Row (
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable { choosingPrimMuscle = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text (
                                modifier = Modifier.padding(4.dp),
                                text = stringResource(R.string.upsert_primary_muscle_caption),
                                color = MaterialTheme.colors.onPrimary
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            StandardTextField(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { choosingPrimMuscle = true },
                                entryValue = selectedPrimMuscle,
                                onValueChange = {},
                                isNumeric = false,
                                readOnly = true,
                                textAlign = TextAlign.Right,
                                trailingIcon = R.drawable.ic_arrow_down
                            )
                            DropdownMenu(
                                modifier = Modifier.height(500.dp),
                                expanded = choosingPrimMuscle,
                                onDismissRequest = { choosingPrimMuscle = false }
                            ) {
                                muscles.forEach { muscle ->
                                    DropdownMenuItem(onClick = {
                                        selectedPrimMuscle = muscle
                                        choosingPrimMuscle = false
                                    } ) {
                                        Text(muscle)
                                    }
                                }
                            }
                        }

                        //Secondary muscle
                        Row (
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable { choosingSecMuscle = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text (
                                modifier = Modifier.padding(4.dp),
                                text = stringResource(R.string.upsert_secondary_muscles_caption),
                                color = MaterialTheme.colors.onPrimary
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            StandardTextField(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { choosingSecMuscle = true },
                                entryValue = selectedSecMuscles,
                                onValueChange = {},
                                isNumeric = false,
                                readOnly = true,
                                textAlign = TextAlign.Right,
                                trailingIcon = R.drawable.ic_arrow_down
                            )
                            DropdownMenu(
                                modifier = Modifier.height(500.dp),
                                expanded = choosingSecMuscle,
                                onDismissRequest = { choosingSecMuscle = false }
                            ) {
                                muscles.forEach { muscle ->
                                    var isSelected by rememberSaveable { mutableStateOf(muscle in selectedSecMuscles) }

                                    DropdownMenuItem(onClick = {
                                        if (isSelected) {
                                            selectedSecMuscles.replace(Regex("$muscle.{2}"), "")
                                            isSelected = false
                                        } else {
                                            selectedSecMuscles += muscle
                                            isSelected = true
                                        }
                                    }) {
                                        Row (
                                            verticalAlignment = Alignment.CenterVertically
                                                ) {
                                            Text(muscle)

                                            Spacer(modifier = Modifier.weight(1f))

                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    if (isSelected) {
                                                        selectedSecMuscles.replace(Regex("$muscle.{2}"), "")
                                                        isSelected = false
                                                    } else {
                                                        selectedSecMuscles += muscle
                                                        isSelected = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                Divider()

                                DropdownMenuItem(onClick = { choosingSecMuscle = false }) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(stringResource(R.string.confirm_button))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

                //Other settings
                Text (
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.upsert_other_options_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                Card (
                    modifier = Modifier
                        .padding(8.dp),
                    backgroundColor = MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.secondary)
                ) {
                    Column {
                        //Category
                        Row (
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable { choosingCategory = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text (
                                modifier = Modifier.padding(4.dp),
                                text = stringResource(R.string.upsert_category_caption),
                                color = MaterialTheme.colors.onPrimary
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            StandardTextField(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { choosingCategory = true },
                                entryValue = selectedCategory,
                                onValueChange = {},
                                isNumeric = false,
                                readOnly = true,
                                textAlign = TextAlign.Right,
                                trailingIcon = R.drawable.ic_arrow_down
                            )
                            DropdownMenu(
                                modifier = Modifier.height(500.dp),
                                expanded = choosingCategory,
                                onDismissRequest = { choosingCategory = false }
                            ) {
                                categories.forEach { currentCat ->
                                    DropdownMenuItem(onClick = {
                                        selectedCategory = currentCat
                                        choosingCategory = false
                                    } ) {
                                        Text(currentCat)
                                    }
                                }
                            }
                        }

                        //Is assisted
                        Row (
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text (
                                modifier = Modifier.padding(4.dp),
                                text = stringResource(R.string.upsert_assisted_caption),
                                color = MaterialTheme.colors.onPrimary
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            RadioButton(
                                enabled = !isBeingEdited,
                                selected = isAssisted,
                                onClick = { isAssisted = !isAssisted}
                            )
                        }
                    }
                }

                //Buttons
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

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton (onClick = {
                        if (checkExerciseEntries(exercise.exercise)) {
                            missingEntriesDialog = true
                        } else {
                            addExercise(
                                ExerciseWithSecMuscle(
                                    exercise = Exercise(
                                        exerciseName = exerciseName,
                                        isDefault = 0,
                                        primMuscle = selectedPrimMuscle,
                                        category = selectedCategory,
                                        isAssisted = if (isAssisted) { 1 } else { 0 }
                                    ),
                                    muscles = /*TODO*/
                                )
                            )
                        }
                    }) {
                        Text (
                            text = stringResource(R.string.confirm_button),
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }
            }

            //Dialogs
            if(missingEntriesDialog) {
                MissingEntriesDialog (onDismissRequest = { missingEntriesDialog = false })
            }
        }
    }
}

@Composable
private fun MissingEntriesDialog(
    onDismissRequest: () -> Unit
) {
    Dialog (onDismissRequest = { onDismissRequest() }) {
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
                    text = stringResource(R.string.confirm_exercise_deletion_message),
                    color = MaterialTheme.colors.onPrimary,
                    textAlign = TextAlign.Center
                )

                TextButton (onClick = { onDismissRequest() }) {
                    Text (
                        text = stringResource(R.string.confirm_button),
                        color = MaterialTheme.colors.secondary
                    )
                }
            }
        }
    }
}

private fun checkExerciseEntries(exercise: Exercise): Boolean {
    return (exercise.exerciseName == "" || exercise.primMuscle == "" || exercise.category == "")
}

@Preview
@Composable
private fun ExerciseCreationDialogPreview() {
    ProgressiveRecordsTheme {
        ExerciseCreationDialog(
            exercise = createEmptyExercise(),
            addExercise = {},
            onDismissRequest = {},
            isBeingEdited = false
        )
    }
}