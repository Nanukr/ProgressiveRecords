package com.rib.progressiverecords.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rib.progressiverecords.ExerciseSortParams
import com.rib.progressiverecords.ExerciseViewModel
import com.rib.progressiverecords.R
import com.rib.progressiverecords.ui.theme.MultipleOptionsChoosingColumn

@Composable
fun ExerciseListFilterDialog(
    viewModel: ExerciseViewModel,
    currentFilters: ExerciseSortParams,
    onDismissRequest: () -> Unit
) {
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
                        selectedOptions = selectedMuscles,
                        addSelectedOption = { selectedMuscles = selectedMuscles + it },
                        removeSelectedOption = { selectedMuscles = selectedMuscles - it }
                    )
                } else {
                    //Category filter
                    MultipleOptionsChoosingColumn(
                        options = categories,
                        selectedOptions = selectedCategories,
                        addSelectedOption = { selectedCategories = selectedCategories + it },
                        removeSelectedOption = { selectedCategories = selectedCategories - it }
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