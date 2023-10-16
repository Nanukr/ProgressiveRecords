package com.rib.progressiverecords.ui.exercise

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rib.progressiverecords.ExerciseSortParams
import com.rib.progressiverecords.viewModel.ExerciseViewModel
import com.rib.progressiverecords.R
import com.rib.progressiverecords.ui.theme.MultipleOptionsChoosingColumn

@Composable
fun ExerciseListFilterDialog(
    viewModel: ExerciseViewModel,
    currentFilters: ExerciseSortParams,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    var musclesSelected by rememberSaveable { mutableStateOf(true) }

    var selectedMuscles by rememberSaveable { mutableStateOf(currentFilters.muscles) }

    var selectedCategories by rememberSaveable { mutableStateOf(currentFilters.categories) }

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
        Card(
            backgroundColor = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                FilterDialogHeader(
                    onMuscleScreen = { musclesSelected = true },
                    onCategoryScreen = { musclesSelected = false }
                )

                Row {
                    Divider(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        color = if (musclesSelected) { MaterialTheme.colors.secondary} else { Color.LightGray }
                    )

                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (!musclesSelected) { MaterialTheme.colors.secondary} else { Color.LightGray }
                    )
                }

                if (musclesSelected) {
                    //Muscle filter
                    MultipleOptionsChoosingColumn(
                        options = muscles,
                        selectedOptions = translateMuscleListFromEnglish(selectedMuscles, context),
                        addSelectedOption = { selectedMuscles = selectedMuscles + getMuscleInEnglish(it, context) },
                        removeSelectedOption = { selectedMuscles = selectedMuscles - getMuscleInEnglish(it, context) }
                    )
                } else {
                    //Category filter
                    MultipleOptionsChoosingColumn(
                        options = categories,
                        selectedOptions = translateCategoryListFromEnglish(selectedCategories, context),
                        addSelectedOption = { selectedCategories = selectedCategories + getCategoryInEnglish(it, context) },
                        removeSelectedOption = { selectedCategories = selectedCategories - getCategoryInEnglish(it, context) }
                    )
                }

                Divider()

                FilterDialogBottom(
                    onClearFilters = {
                        selectedMuscles = emptyList()
                        selectedCategories = emptyList()
                    },
                    onApplyChanges = {
                        viewModel.onChangeFilterMuscles(selectedMuscles)
                        viewModel.onChangeFilterCategories(selectedCategories)
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterDialogHeader(
    onMuscleScreen: () -> Unit,
    onCategoryScreen: () -> Unit
) {
    Row (
        modifier = Modifier
            .padding(8.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .clickable { onMuscleScreen() }
            ,
            text = stringResource(R.string.upsert_target_muscles_caption),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .clickable { onCategoryScreen() }
            ,
            text = stringResource(R.string.upsert_category_caption),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FilterDialogBottom(
    onClearFilters: () -> Unit,
    onApplyChanges: () -> Unit
) {
    Row (
        modifier = Modifier
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = {
            onClearFilters()
        }
        ) {
            Text(
                text = stringResource(R.string.clear_filter_button),
                color = MaterialTheme.colors.secondary
            )
        }

        Spacer (modifier = Modifier.weight(1f))

        TextButton(onClick = { onApplyChanges() }) {
            Text(
                text = stringResource(R.string.confirm_button),
                color = MaterialTheme.colors.secondary
            )
        }
    }
}

fun getMuscleInEnglish(
    muscleResource: String,
    context: Context
): String {
    return when (muscleResource) {
        context.getString(R.string.muscle_name_chest) -> "Chest"
        context.getString(R.string.muscle_name_biceps) -> "Biceps"
        context.getString(R.string.muscle_name_triceps) -> "Triceps"
        context.getString(R.string.muscle_name_forearms) -> "Forearms"
        context.getString(R.string.muscle_name_core) -> "Core"
        context.getString(R.string.muscle_name_traps) -> "Traps"
        context.getString(R.string.muscle_name_lats) -> "Lats"
        context.getString(R.string.muscle_name_shoulders) -> "Shoulders"
        context.getString(R.string.muscle_name_upper_back) -> "Upper Back"
        context.getString(R.string.muscle_name_lower_back) -> "Lower Back"
        context.getString(R.string.muscle_name_quads) -> "Quads"
        context.getString(R.string.muscle_name_glutes) -> "Glutes"
        context.getString(R.string.muscle_name_hamstrings) -> "Hamstrings"
        context.getString(R.string.muscle_name_calves) -> "Calves"
        context.getString(R.string.muscle_name_adductors) -> "Adductors"
        context.getString(R.string.muscle_name_abductors) -> "Abductors"
        context.getString(R.string.muscle_name_full_body) -> "Full Body"
        else -> "Olympic"
    }
}

fun getCategoryInEnglish(
    categoryResource: String,
    context: Context
): String {
    return when (categoryResource) {
        context.getString(R.string.category_name_dumbbells) -> "Dumbbells"
        context.getString(R.string.category_name_barbell) -> "Barbell"
        context.getString(R.string.category_name_weighted_bodyweight) -> "Weighted Bodyweight"
        context.getString(R.string.category_name_assisted_bodyweight) -> "Assisted Bodyweight"
        context.getString(R.string.category_name_resistance_bands) -> "Resistance Bands"
        context.getString(R.string.category_name_kettlebells) -> "Kettlebells"
        context.getString(R.string.category_name_trap_bar) -> "Trap Bar"
        context.getString(R.string.category_name_smith_machine) -> "Smith machine"
        context.getString(R.string.category_name_machine) -> "Machine"
        context.getString(R.string.category_name_cable) -> "Cable"
        context.getString(R.string.category_name_cardio) -> "Cardio"
        context.getString(R.string.category_name_duration) -> "Duration"
        context.getString(R.string.category_name_reps_only) -> "Reps Only"
        else -> "Other"
    }
}

fun getMuscleFromEnglish(
    muscleResource: String,
    context: Context
): String {
    return when (muscleResource) {
        "Chest" -> context.getString(R.string.muscle_name_chest)
        "Biceps" -> context.getString(R.string.muscle_name_biceps)
        "Triceps" -> context.getString(R.string.muscle_name_triceps)
        "Forearms" -> context.getString(R.string.muscle_name_forearms)
        "Core" -> context.getString(R.string.muscle_name_core)
        "Traps" -> context.getString(R.string.muscle_name_traps)
        "Lats" -> context.getString(R.string.muscle_name_lats)
        "Shoulders" -> context.getString(R.string.muscle_name_shoulders)
        "Upper Back" -> context.getString(R.string.muscle_name_upper_back)
        "Lower Back" -> context.getString(R.string.muscle_name_lower_back)
        "Quads" -> context.getString(R.string.muscle_name_quads)
        "Glutes" -> context.getString(R.string.muscle_name_glutes)
        "Hamstrings" -> context.getString(R.string.muscle_name_hamstrings)
        "Calves" -> context.getString(R.string.muscle_name_calves)
        "Adductors" -> context.getString(R.string.muscle_name_adductors)
        "Abductors" -> context.getString(R.string.muscle_name_abductors)
        "Full Body" -> context.getString(R.string.muscle_name_full_body)
        else -> context.getString(R.string.muscle_name_olympic)
    }
}

fun getCategoryFromEnglish(
    categoryResource: String,
    context: Context
): String {
    return when (categoryResource) {
        "Dumbbells" -> context.getString(R.string.category_name_dumbbells)
        "Barbell" -> context.getString(R.string.category_name_barbell)
        "Weighted Bodyweight" -> context.getString(R.string.category_name_weighted_bodyweight)
        "Assisted Bodyweight" -> context.getString(R.string.category_name_assisted_bodyweight)
        "Resistance Bands" -> context.getString(R.string.category_name_resistance_bands)
        "Kettlebells" -> context.getString(R.string.category_name_kettlebells)
        "Trap Bar" -> context.getString(R.string.category_name_trap_bar)
        "Smith machine" -> context.getString(R.string.category_name_smith_machine)
        "Machine" -> context.getString(R.string.category_name_machine)
        "Cable" -> context.getString(R.string.category_name_cable)
        "Cardio" -> context.getString(R.string.category_name_cardio)
        "Duration" -> context.getString(R.string.category_name_duration)
        "Reps Only" -> context.getString(R.string.category_name_reps_only)
        else -> context.getString(R.string.category_name_other)
    }
}

fun translateMuscleListFromEnglish(
    muscles: List<String>,
    context: Context
): List<String> {
    val translatedItems: MutableList<String> = mutableListOf()

    muscles.forEach { muscle ->
        translatedItems.add(getMuscleFromEnglish(muscle, context))
    }

    return translatedItems.toList()
}

fun translateCategoryListFromEnglish(
    categories: List<String>,
    context: Context
): List<String> {
    val translatedItems: MutableList<String> = mutableListOf()

    categories.forEach { category ->
        translatedItems.add(getCategoryFromEnglish(category, context))
    }

    return translatedItems.toList()
}