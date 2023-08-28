package com.rib.progressiverecords.ui

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
import com.rib.progressiverecords.model.Muscle
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

    var exerciseName by rememberSaveable{ mutableStateOf(exercise.exercise.exerciseName) }

    var isAssisted = false

    var secMuscles = ""

    for (muscle in exercise.muscles) {
        secMuscles += if (muscle.muscleName != exercise.muscles[exercise.muscles.lastIndex].muscleName) {
            "${muscle.muscleName}, "
        } else {
            muscle.muscleName
        }
    }

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
                Row (
                    modifier = Modifier
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                        )
                {
                    Text (
                        modifier = Modifier.padding(4.dp),
                        text = stringResource(R.string.upsert_exercise_name_caption),
                        color = MaterialTheme.colors.onPrimary
                    )

                    StandardTextField(
                        entryValue = exerciseName,
                        onValueChange = {
                            exerciseName = it
                            exercise.exercise.exerciseName = it
                        },
                        isNumeric = false,
                        modifier = Modifier.padding(4.dp),
                        textAlign = TextAlign.Right
                    )
                }

                //Primary muscle
                Row (
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { choosingPrimMuscle = true },
                    verticalAlignment = Alignment.CenterVertically
                        ) {
                    Text (
                        modifier = Modifier.padding(4.dp),
                        text = stringResource(R.string.upsert_primary_muscle_caption),
                        color = MaterialTheme.colors.onPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text (
                        modifier = Modifier.padding(4.dp),
                        text = exercise.exercise.primMuscle,
                        color = MaterialTheme.colors.onPrimary
                    )
                }

                //Secondary muscle
                Row (
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { choosingSecMuscle = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text (
                        modifier = Modifier.padding(4.dp),
                        text = stringResource(R.string.upsert_secondary_muscles_caption),
                        color = MaterialTheme.colors.onPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text (
                        modifier = Modifier.padding(4.dp),
                        text = secMuscles,
                        color = MaterialTheme.colors.onPrimary
                    )
                }

                //Category
                Row (
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { choosingCategory = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text (
                        modifier = Modifier.padding(4.dp),
                        text = stringResource(R.string.upsert_category_caption),
                        color = MaterialTheme.colors.onPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text (
                        modifier = Modifier.padding(4.dp),
                        text = exercise.exercise.category,
                        color = MaterialTheme.colors.onPrimary
                    )
                }

                //Is assisted
                Row (
                    modifier = Modifier
                        .padding(8.dp),
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
                            addExercise(exercise)
                        }
                    }) {
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