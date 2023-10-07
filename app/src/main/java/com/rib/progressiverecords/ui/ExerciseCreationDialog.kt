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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rib.progressiverecords.R
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Muscle
import com.rib.progressiverecords.model.relations.ExerciseWithSecMuscle
import com.rib.progressiverecords.ui.theme.*

@Composable
fun ExerciseCreationDialog(
    exercise: ExerciseWithSecMuscle,
    addExercise: (ExerciseWithSecMuscle) -> Unit,
    onDismissRequest: () -> Unit
) {
    var choosingPrimMuscle by rememberSaveable { mutableStateOf(false) }
    var choosingSecMuscle by rememberSaveable { mutableStateOf(false) }
    var choosingCategory by rememberSaveable { mutableStateOf(false) }
    var missingEntriesDialog by rememberSaveable { mutableStateOf(false) }

    var exerciseName by rememberSaveable{ mutableStateOf(exercise.exercise.exerciseName) }

    var selectedPrimMuscle by rememberSaveable { mutableStateOf(exercise.exercise.primMuscle) }
    var selectedSecMuscles by rememberSaveable { mutableStateOf(turnMuscleListToStringList(exercise.muscles)) }

    var secMusclesString by rememberSaveable { mutableStateOf(turnStringListToString(selectedSecMuscles)) }

    var selectedCategory by rememberSaveable { mutableStateOf(exercise.exercise.category) }

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
        stringResource(R.string.muscle_name_glutes),
        stringResource(R.string.muscle_name_hamstrings),
        stringResource(R.string.muscle_name_calves),
        stringResource(R.string.muscle_name_adductors),
        stringResource(R.string.muscle_name_abductors),
        stringResource(R.string.muscle_name_full_body),
        stringResource(R.string.muscle_name_olympic),
    )

    val categories = listOf(
        stringResource(R.string.category_name_dumbbells),
        stringResource(R.string.category_name_barbell),
        stringResource(R.string.category_name_weighted_bodyweight),
        stringResource(R.string.category_name_assisted_bodyweight),
        stringResource(R.string.category_name_resistance_bands),
        stringResource(R.string.category_name_kettlebells),
        stringResource(R.string.category_name_trap_bar),
        stringResource(R.string.category_name_smith_machine),
        stringResource(R.string.category_name_machine),
        stringResource(R.string.category_name_cable),
        stringResource(R.string.category_name_cardio),
        stringResource(R.string.category_name_duration),
        stringResource(R.string.category_name_reps_only),
        stringResource(R.string.category_name_other),
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
                //Header
                ExerciseCreationHeader()

                //Exercise name
                ExerciseNameEntry(
                    exerciseName = exerciseName,
                    onChangeExerciseName = { exerciseName = it }
                )

                Spacer(modifier = Modifier.padding(8.dp))

                //Muscles
                Text (
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.upsert_target_muscles_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                ExerciseMusclesEntries(
                    selectedPrimMuscle = selectedPrimMuscle,
                    secMusclesString = secMusclesString,
                    onChoosePrimMuscle = { choosingPrimMuscle = true },
                    onChooseSecMuscles = { choosingSecMuscle = true }
                )

                Spacer(modifier = Modifier.padding(8.dp))

                //Other settings
                Text (
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.upsert_other_options_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                ExerciseOthersEntries(
                    selectedCategory = selectedCategory,
                    onChooseCategory = { choosingCategory = true }
                )

                //Buttons
                CreateExerciseDialogButtons(
                    exerciseName = exerciseName,
                    selectedPrimMuscle = selectedPrimMuscle,
                    selectedSecMuscles = selectedSecMuscles,
                    selectedCategory = selectedCategory,
                    onMissingEntries = { missingEntriesDialog = true },
                    addExercise = { addExercise(it) },
                    onDismissRequest = { onDismissRequest() }
                )
            }

            //Dialogs
            if (choosingPrimMuscle) {
                SingleOptionChoosingDialog(
                    options = muscles,
                    selectedOption = selectedPrimMuscle,
                    title = R.string.upsert_primary_muscle_long_caption,
                    changeSelectedOption = { selectedPrimMuscle = it },
                    onDismissRequest = { choosingPrimMuscle = false }
                )
            }

            if (choosingSecMuscle) {
                MultipleOptionsChoosingDialog(
                    title = R.string.upsert_secondary_muscles_long_caption,
                    options = muscles,
                    selectedOptions = selectedSecMuscles,
                    addSelectedOption = {
                        selectedSecMuscles = selectedSecMuscles + it
                        secMusclesString = turnStringListToString(selectedSecMuscles)
                    },
                    removeSelectedOption = {
                        selectedSecMuscles = selectedSecMuscles - it
                        secMusclesString = turnStringListToString(selectedSecMuscles)
                    },
                    onDismissRequest = { choosingSecMuscle = false }
                )
            }

            if (choosingCategory) {
                SingleOptionChoosingDialog(
                    options = categories,
                    selectedOption = selectedCategory,
                    title = R.string.upsert_primary_muscle_long_caption,
                    changeSelectedOption = { selectedCategory = it },
                    onDismissRequest = { choosingCategory = false }
                )
            }

            if (missingEntriesDialog) {
                MissingEntriesDialog (onDismissRequest = { missingEntriesDialog = false })
            }
        }
    }
}

@Composable
private fun ExerciseCreationHeader() {
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
}

@Composable
private fun ExerciseNameEntry(
    exerciseName: String,
    onChangeExerciseName: (String) -> Unit
) {
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
                onChangeExerciseName(it)
            },
            isNumeric = false,
            modifier = Modifier.padding(4.dp),
            textAlign = TextAlign.Left
        )
    }
}

@Composable
private fun ExerciseMusclesEntries(
    selectedPrimMuscle: String,
    secMusclesString: String,
    onChoosePrimMuscle: () -> Unit,
    onChooseSecMuscles: () -> Unit,
) {
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
                    .clickable { onChoosePrimMuscle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    modifier = Modifier.padding(4.dp),
                    text = stringResource(R.string.upsert_primary_muscle_short_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.padding(16.dp),
                    text = selectedPrimMuscle,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = "",
                    tint = MaterialTheme.colors.onPrimary
                )
            }

            //Secondary muscle
            Row (
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { onChooseSecMuscles() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    modifier = Modifier.padding(4.dp),
                    text = stringResource(R.string.upsert_secondary_muscles_short_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.padding(16.dp),
                    text = secMusclesString,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = "",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ExerciseOthersEntries(
    selectedCategory: String,
    onChooseCategory: () -> Unit
) {
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
                    .clickable { onChooseCategory() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    modifier = Modifier.padding(4.dp),
                    text = stringResource(R.string.upsert_category_caption),
                    color = MaterialTheme.colors.onPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.padding(16.dp),
                    text = selectedCategory,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = "",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}

@Composable
private fun CreateExerciseDialogButtons(
    exerciseName: String,
    selectedPrimMuscle: String,
    selectedSecMuscles: List<String>,
    selectedCategory: String,
    onMissingEntries: () -> Unit,
    addExercise: (ExerciseWithSecMuscle) -> Unit,
    onDismissRequest: () -> Unit
) {
    val newExercise = Exercise(
        exerciseName = exerciseName,
        primMuscle = selectedPrimMuscle,
        category = selectedCategory,
        isDefault = 0
    )

    Row (
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        //Cancel button
        TextButton (onClick = { onDismissRequest() }) {
            Text (
                text = stringResource(R.string.cancel_button),
                color = MaterialTheme.colors.secondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        //Confirm button
        TextButton (onClick = {
            if (checkExerciseEntries(newExercise)) {
                onMissingEntries()
            } else {
                addExercise(
                    ExerciseWithSecMuscle(
                        exercise = newExercise,
                        muscles = turnStringListToMuscleList(selectedSecMuscles)
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
@Composable
private fun MissingEntriesDialog(
    onDismissRequest: () -> Unit
) {
    StandardAlertDialog(
        title = stringResource(R.string.missing_entries_dialog_message),
        onDismissRequest = { onDismissRequest() }
    )
}

//Functions
private fun checkExerciseEntries(exercise: Exercise): Boolean {
    return (exercise.exerciseName == "" || exercise.primMuscle == "" || exercise.category == "")
}

private fun turnStringListToString(strings: List<String>): String {
    var muscleString = ""

    for (string in strings) {
        muscleString += if (string != strings[strings.lastIndex]) {
            "$string, "
        } else {
            string
        }
    }

    return muscleString
}

private fun turnMuscleListToStringList(muscles: List<Muscle>): List<String> {
    val stringList: MutableList<String> = mutableListOf()

    muscles.forEach {
        stringList.add(it.muscleName)
    }

    return stringList.toList()
}

private fun turnStringListToMuscleList(strings: List<String>): List<Muscle> {
    val muscleList: MutableList<Muscle> = mutableListOf()

    strings.forEach {
        muscleList.add(Muscle(it))
    }

    return muscleList.toList()
}

@Preview
@Composable
private fun ExerciseCreationDialogPreview() {
    ProgressiveRecordsTheme {
        ExerciseCreationDialog(
            exercise = createEmptyExercise(),
            addExercise = {},
            onDismissRequest = {}
        )
    }
}