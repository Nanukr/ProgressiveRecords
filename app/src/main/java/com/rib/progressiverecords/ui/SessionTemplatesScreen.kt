package com.rib.progressiverecords.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rib.progressiverecords.ExerciseSetsList
import com.rib.progressiverecords.R
import com.rib.progressiverecords.SessionCreationVariation
import com.rib.progressiverecords.SessionViewModel
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.relations.SessionWithRecords
import com.rib.progressiverecords.ui.theme.StandardButton
import com.rib.progressiverecords.ui.theme.StandardTwoButtonsDialog
import java.util.*

@Composable
fun SessionTemplatesScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    var startingCustomWorkout by rememberSaveable { mutableStateOf(false) }

    var deletingTemplate by rememberSaveable { mutableStateOf(false) }

    var selectedTemplate: SessionWithRecords? = null

    var templateBeingDeleted: Session? = null

    Scaffold(
        topBar = { TopBar(
            onClick = {},
            title = stringResource(R.string.train_nav_item_text),
            contentDescription = stringResource(R.string.create_session_icon_description)
        ) }
    ) { it
        TemplatesList(
            viewModel = viewModel,
            navController = navController,
            onSelectSession = {
                startingCustomWorkout = true
                selectedTemplate = it
            },
            onCreateNewTemplate = {
                viewModel.variation = SessionCreationVariation.TEMPLATE
                navController.navigate("session_creation")
            },
            onEditTemplate = { template ->
                viewModel.createdSession = template.session
                viewModel.newRecords = template.records
                viewModel.checkedRecords = template.records
                viewModel.variation = SessionCreationVariation.EDIT_TEMPLATE
                navController.navigate("session_creation")
            },
            onDeleteTemplate = {
                templateBeingDeleted = it
                deletingTemplate = true
            }
        )

        if (startingCustomWorkout) {
            selectedTemplate?.let { template ->
                StartCustomWorkoutPreviewDialog(
                    template = template,
                    onStartCustomWorkout = {
                        viewModel.createdSession = Session(
                            id = UUID.randomUUID(),
                            sessionName = it.session.sessionName,
                            date = Date(),
                            isTemplate = 0
                        )
                        viewModel.templateRecords = it.records
                        viewModel.variation = SessionCreationVariation.CUSTOM
                        navController.navigate("session_creation")
                    },
                    onDismissRequest = {
                        startingCustomWorkout = false
                        selectedTemplate = null
                    }
                )
            }
        }

        if (deletingTemplate) {
            DeleteTemplateDialog(
                onDeleteTemplate = {
                    deleteSessionWithRecords(
                        session = templateBeingDeleted!!,
                        viewModel = viewModel,
                        navController = navController,
                        isTemplate = true
                    )
                },
                onDismissRequest = { deletingTemplate = false }
            )
        }
    }
    BackHandler {}
}

@Composable
private fun TemplatesList(
    viewModel: SessionViewModel,
    navController: NavController,
    onSelectSession: (SessionWithRecords) -> Unit,
    onCreateNewTemplate: () -> Unit,
    onEditTemplate: (SessionWithRecords) -> Unit,
    onDeleteTemplate: (Session) -> Unit
) {
    Column {
        StartEmptyWorkoutButton {
            viewModel.variation = SessionCreationVariation.GENERAL
            navController.navigate("session_creation")
        }

        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .weight(1f),
                text = stringResource(R.string.template_list_header),
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                modifier = Modifier.padding(8.dp),
                onClick = {
                    onCreateNewTemplate()
                }
            ) {
                Icon(
                    painterResource(R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        SessionList(
            viewModel = viewModel,
            areTemplates = true,
            onSelectSession = {
                onSelectSession(it)
            },
            onEditTemplate = { onEditTemplate(it) },
            onDeleteTemplate = { onDeleteTemplate(it) }
        )
    }
}

//Buttons
@Composable
private fun StartEmptyWorkoutButton (
    onStartEmptyWorkout: () -> Unit
) {
    StandardButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        onClick = { onStartEmptyWorkout() },
        text = stringResource(R.string.start_empty_workout_button),
        backgroundColor = MaterialTheme.colors.secondaryVariant,
        textColor = MaterialTheme.colors.onSecondary
    )
}

//Dialogs
@Composable
private fun StartCustomWorkoutPreviewDialog(
    template: SessionWithRecords,
    onStartCustomWorkout: (SessionWithRecords) -> Unit,
    onDismissRequest: () -> Unit
) {
    var records by remember { mutableStateOf((template.records)) }
    records = records.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    )

    LaunchedEffect(template) {
        records = template.records
    }

    val exerciseSetsList = ExerciseSetsList().organizeRecords(records).totalSets

    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        text = {
            Column {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = template.session.sessionName,
                    style = MaterialTheme.typography.h6
                )

                Divider()

                exerciseSetsList.forEach {set ->
                    if (set.isNotEmpty()) {
                        val lastSet = set[set.lastIndex]
                        Row {
                            Text(
                                text = lastSet.exerciseName,
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.padding(4.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "x ${lastSet.setNumber}",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            StandardButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.start_custom_workout_button),
                onClick = { onStartCustomWorkout(template) }
            )
        }
    )
}

@Composable
private fun DeleteTemplateDialog(
    onDeleteTemplate: () -> Unit,
    onDismissRequest: () -> Unit
) {
    StandardTwoButtonsDialog(
        title = stringResource(R.string.delete_template_dialog_title),
        text = stringResource(R.string.delete_item_dialog_text),
        onConfirm = { onDeleteTemplate() },
        onDismissRequest = { onDismissRequest() }
    )
}