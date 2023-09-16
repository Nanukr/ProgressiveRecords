package com.rib.progressiverecords.ui

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rib.progressiverecords.*
import com.rib.progressiverecords.R
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.TimeLength
import com.rib.progressiverecords.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun SessionCreationScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    val addingExercise = rememberSaveable { mutableStateOf(false) }

    var deletingExercise by rememberSaveable { mutableStateOf(false) }

    val changingDate = rememberSaveable { mutableStateOf(false) }

    val savingSession = rememberSaveable { mutableStateOf(false) }

    var session = viewModel.createdSession ?: Session(
        id = UUID.randomUUID(),
        date = Date(),
        sessionName = stringResource(R.string.new_session_name)
    )
    viewModel.createdSession = session

    var records by remember { mutableStateOf((viewModel.newRecords)) }
    records = records.sortedWith(
        compareBy<Record> { it.sessionPosition }
            .thenBy { it.setNumber }
    )

    val exerciseSetsList = ExerciseSetsList().organizeRecords(records)

    Scaffold (
        topBar = { TopBar(
            onClick = { savingSession.value = true },
            icon = painterResource(R.drawable.ic_save),
            contentDescription = stringResource(R.string.save_session_icon_description)
        )}
            ) { it
        SetList(
            session = session,
            exerciseSetsList = exerciseSetsList,
            viewModel = viewModel,
            navController = navController,
            onUpdateRecords = { record ->
                val foundRecord = records.find { it.exerciseName == record.exerciseName && it.setNumber == record.setNumber }
                records = if (foundRecord == null) {
                    records + record
                } else {
                    records - foundRecord + record
                }
                viewModel.newRecords = records
                              },
            onOpenDateDialog = { changingDate.value = true },
            onUpdateSessionName = {
                session = session.copy(
                    id = session.id,
                    sessionName = it,
                    date = session.date
                )
                viewModel.createdSession = session
            },
            selectExercise = { addingExercise.value = true },
            onDeleteExercise = { deletingExercise = true }
        )

        if (changingDate.value) {
            SessionDatePickerDialog(
                onDismissRequest = { changingDate.value = false },
                onSelectDate = {
                    if (it != null) {
                        session = session.copy(
                            id = session.id,
                            sessionName = session.sessionName,
                            date = Date(it)
                        )
                        viewModel.createdSession = session
                    }
                }
            )
        }

        if (savingSession.value) {
            SaveSessionDialog(
                onDismissRequest = { savingSession.value = false },
                onSessionSaved = {
                    saveSessionToDb(
                        viewModel = viewModel,
                        navController = navController,
                        records = records,
                        session = session
                    )
                    savingSession.value = false
                },
                viewModel = viewModel
            )
        }

        if (addingExercise.value) {
            SelectExerciseDialog(
                onDismissRequest = { addingExercise.value = false },
                onExerciseSelected = { exercises ->
                    addingExercise.value = false

                    if (exercises.isNotEmpty()) {
                        exercises.forEach { exercise ->
                            val lastExercisePosition = if (records.isEmpty()) { 0 } else { records[records.lastIndex].sessionPosition }

                            records = records + createNewRecord(
                                sessionId = session.id,
                                previousSet = 0,
                                exerciseName = exercise.exerciseName,
                                sessionPosition = lastExercisePosition + 1,
                                category = exercise.category
                            )
                            viewModel.newRecords = records
                        }
                    }
                }
            )
        }

        if (deletingExercise) {
            DeleteExerciseDialog(
                onDismissRequest = {
                    deletingExercise = false
                    viewModel.positionBeingModified = null
                },
                onExerciseDeleted = {
                    deletingExercise = false
                    records = deleteSetInPosition(viewModel = viewModel)
                }
            )
        }
    }

    BackHandler {
        cancelAndDelete(viewModel = viewModel, navController = navController)
    }
}

@Composable
private fun SetList(
    session: Session,
    exerciseSetsList: ExerciseSetsList,
    onUpdateRecords: (Record) -> Unit,
    onUpdateSessionName: (String) -> Unit,
    onOpenDateDialog: () -> Unit,
    selectExercise: () -> Unit,
    onDeleteExercise: () -> Unit,
    viewModel: SessionViewModel,
    navController: NavController
) {
    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        SessionNameAndDate(
            session = session,
            onOpenDateDialog = { onOpenDateDialog() },
            onUpdateSessionName = { onUpdateSessionName(it) }
        )

        if (exerciseSetsList.totalSets.isEmpty()) {
            AddExerciseButton(selectExercise = { selectExercise() })

            CancelButton(viewModel = viewModel, navController = navController)
        } else {
            LazyColumn {
                items(exerciseSetsList.totalSets) {setList  ->
                    if (setList.isNotEmpty()) {
                        ExerciseSets(
                            sets = setList,
                            sessionId = session.id,
                            onUpdateRecords = { onUpdateRecords(it) },
                            onDeleteExercise = {
                                viewModel.positionBeingModified = it
                                onDeleteExercise()
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    AddExerciseButton(selectExercise = { selectExercise() })
                }

                item {
                    CancelButton(viewModel = viewModel, navController = navController)
                }
            }
        }
    }
}

@Composable
private fun SessionNameAndDate (
    session: Session,
    onOpenDateDialog: () -> Unit,
    onUpdateSessionName: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(session.sessionName) }

    Row (
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        StandardTextField(
            entryValue = name,
            onValueChange = {
                            name = it
                onUpdateSessionName(name)
            },
            isNumeric = false,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .shadow(5.dp, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.primary)
        )

        StandardButton(
            onClick = { onOpenDateDialog() },
            text = DateFormat.format("dd / MMM / yyyy", session.date).toString(),
            textAlign = TextAlign.Right
        )
    }
}

@Composable
private fun ExerciseHeader (
    exerciseName: String,
    sessionPosition: String,
    onChangeExercise: () -> Unit,
    onDeleteExercise: () -> Unit
) {
    var dropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column (
        modifier = Modifier
            .padding(vertical = 4.dp)
            ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(4.dp),
                text = "$sessionPosition: $exerciseName",
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(modifier = Modifier
                .wrapContentSize(Alignment.TopStart)) {
                IconButton(onClick = { dropdownMenuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = ""
                    )
                }
                DropdownMenu(
                    expanded = dropdownMenuExpanded,
                    onDismissRequest = { dropdownMenuExpanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        onChangeExercise()
                        dropdownMenuExpanded = false
                    }) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            painter = painterResource(id = R.drawable.ic_swap),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onPrimary
                        )

                        Text(stringResource(R.string.change_exercise_button))
                    }

                    DropdownMenuItem(onClick = {
                        onDeleteExercise()
                        dropdownMenuExpanded = false
                    }) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onPrimary
                        )

                        Text(stringResource(R.string.delete_exercise_icon_description))
                    }
                }
            }
        }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
                ) {
            Text (
                modifier = Modifier
                    .weight(0.5f)
                    .padding(4.dp),
                text = stringResource(R.string.set_label),
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text (
                text = stringResource(R.string.weight_label),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.repetitions_label),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
private fun ExerciseSets(
    sets: List<Record>,
    sessionId: UUID,
    onUpdateRecords: (Record) -> Unit,
    onDeleteExercise: (Int) -> Unit
) {
    val exerciseName = sets[0].exerciseName
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(color = MaterialTheme.colors.primary)
    ) {
        ExerciseHeader(
            exerciseName = exerciseName,
            sessionPosition = sets[0].sessionPosition.toString(),
            onChangeExercise = { /*TODO*/ },
            onDeleteExercise = { onDeleteExercise(sets[0].sessionPosition) }
        )

        Divider(modifier = Modifier.padding(8.dp))

        sets.forEach { record ->
            val recordIsSaved = rememberSaveable { mutableStateOf (false) }
            SetItem(
                record = record,
                onChangeRecordState = {
                    recordIsSaved.value = ! recordIsSaved.value
                    if (recordIsSaved.value) {
                        onUpdateRecords(it)
                    }
                                      },
                recordIsSaved = recordIsSaved.value
            )
        }

        AddSetButton(
            sessionId = sessionId,
            previousSet = sets.last().setNumber,
            exerciseName = exerciseName,
            onExerciseAdded = { onUpdateRecords(it) },
            sessionPosition = sets[0].sessionPosition
        )
    }
}

@Composable
private fun SetItem(
    record: Record,
    onChangeRecordState: (Record) -> Unit,
    recordIsSaved: Boolean
) {
    val currentRecord = remember { mutableStateOf(record) }

    var repetitions by rememberSaveable{ mutableStateOf(record.repetitions.toString()) }

    var weight by rememberSaveable { mutableStateOf(record.weight.toString()) }

    Row (
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
            ) {
        Text (
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)
                .padding(4.dp),
            text = record.setNumber.toString(),
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center,
        )

        StandardTextField(
            entryValue = weight,
            onValueChange = { weight = it },
            modifier = Modifier
                .weight(1f),
            isNumeric = true,
            isEnabled = !recordIsSaved,
            backgroundColor = MaterialTheme.colors.background,
            textColor = MaterialTheme.colors.onPrimary
        )

        StandardTextField(
            entryValue = repetitions,
            onValueChange = { repetitions = it },
            modifier = Modifier
                .weight(1f),
            isNumeric = true,
            isEnabled = !recordIsSaved,
            backgroundColor = MaterialTheme.colors.background,
            textColor = MaterialTheme.colors.onPrimary
        )

        Checkbox(
            checked = recordIsSaved,
            onCheckedChange = {
                if (!recordIsSaved) {
                    currentRecord.value.weight = weight.toFloat()
                    currentRecord.value.repetitions = repetitions.toInt()
                }
                onChangeRecordState(currentRecord.value)
            },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.secondary),
            modifier = Modifier.weight(0.5f)
        )
    }
}

@Composable
private fun AddSetButton(
    sessionId: UUID,
    previousSet: Int,
    exerciseName: String,
    sessionPosition: Int,
    onExerciseAdded: (Record) -> Unit
) {
    StandardButton (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = { onExerciseAdded(
            createNewRecord(
                sessionId = sessionId,
                previousSet = previousSet,
                exerciseName = exerciseName,
                sessionPosition = sessionPosition,
                category = "standard"
            )
        ) },
        text = stringResource(R.string.add_set_button),
        backgroundColor = MaterialTheme.colors.secondaryVariant,
        textColor = MaterialTheme.colors.onSecondary
    )
}

@Composable
private fun AddExerciseButton(
    selectExercise: () -> Unit
) {
    StandardButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = { selectExercise() },
        text = stringResource(R.string.add_exercise_button),
        backgroundColor = MaterialTheme.colors.secondaryVariant,
        textColor = MaterialTheme.colors.onSecondary
    )
}

@Composable
private fun CancelButton (
    viewModel: SessionViewModel,
    navController: NavController
) {
    StandardButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = { cancelAndDelete(viewModel, navController) },
        text = stringResource(R.string.cancel_session_button),
        backgroundColor = Color.Red,
        textColor = Color.White
    )
}

@Composable
private fun SelectExerciseDialog (
    onDismissRequest: () -> Unit,
    onExerciseSelected: (List<Exercise>) -> Unit
) {
    Dialog (
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismissRequest() }
    ) {
        Surface (modifier = Modifier.fillMaxSize()) {
            ExerciseListScreen(
                viewModel = ExerciseViewModel(),
                isBeingSelected = true,
                onExercisesSelected = { onExerciseSelected(it) }
            )
        }
    }
}

@Composable
private fun SaveSessionDialog (
    onDismissRequest: () -> Unit,
    onSessionSaved: () -> Unit,
    viewModel: SessionViewModel
) {
    Dialog (onDismissRequest = { onDismissRequest() } ) {
        Card (
            backgroundColor = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(8.dp)
                ) {
            if (viewModel.newRecords.isNotEmpty()) {
                Column (modifier = Modifier.padding(16.dp)) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = stringResource(R.string.save_session_top_message),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = stringResource(R.string.save_session_bottom_message),
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    AlertDialogButtons(
                        onCancel = { onDismissRequest() },
                        onConfirm = { onSessionSaved() }
                    )
                }

            } else {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = stringResource(R.string.save_session_error_message),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    TextButton (
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel_button),
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteExerciseDialog (
    onDismissRequest: () -> Unit,
    onExerciseDeleted: () -> Unit,
) {
    AlertDialog(
        title = {
            Text (
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.delete_exercise_dialog_title),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text (
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.delete_item_dialog_text),
                textAlign = TextAlign.Center
            )
        },
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = { onDismissRequest() },
        buttons = {
            AlertDialogButtons(
                onCancel = { onDismissRequest() },
                onConfirm = { onExerciseDeleted() }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDatePickerDialog(
    onDismissRequest: () -> Unit,
    onSelectDate: (Long?) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
        modifier = Modifier.padding(16.dp),

        onDismissRequest = { onDismissRequest() },

        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    onSelectDate(datePickerState.selectedDateMillis)
                },
                enabled = confirmEnabled.value
            ) {
                Text(
                    text = stringResource(R.string.confirm_button),
                    color = MaterialTheme.colors.secondary
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text(
                    text = stringResource(R.string.cancel_button),
                    color = MaterialTheme.colors.secondary
                )
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun saveSessionToDb (
    viewModel: SessionViewModel,
    navController: NavController,
    records: List<Record>,
    session: Session
) {
    viewModel.viewModelScope.launch {
        viewModel.addSession(session)

        records.forEach {record ->
            viewModel.addRecord(record)
        }
    }
    viewModel.newRecords = emptyList()
    navController.navigate("session_list")
}

private fun cancelAndDelete(
    viewModel: SessionViewModel,
    navController: NavController
) {
    viewModel.newRecords = emptyList()
    navController.navigate("session_list")
}

private fun createNewRecord(
    sessionId: UUID,
    previousSet: Int,
    exerciseName: String,
    sessionPosition: Int,
    category: String
): Record {
    val reps = if (category != "Duration" && category != "Cardio") { 0 } else { null }
    val weight = if (category != "Reps Only" && category != "Duration" && category != "Cardio") { 0f } else { null }
    val duration = if (category == "Duration" || category == "Cardio") { TimeLength(0, 0, 0) } else { null }
    val distance = if (category == "Cardio") { 0.0f } else { null }

    return Record(
        id = UUID.randomUUID(),
        sessionId = sessionId,
        exerciseName = exerciseName,
        sessionPosition = sessionPosition,
        setNumber = previousSet + 1,
        repetitions = reps,
        weight = weight,
        exerciseDuration = duration,
        distance = distance
    )
}

private fun deleteSetInPosition(
    viewModel: SessionViewModel
): List<Record> {

    viewModel.newRecords.forEach { record ->
        if (record.sessionPosition == viewModel.positionBeingModified) {
            viewModel.newRecords -= record
        }

        if (record.sessionPosition > viewModel.positionBeingModified!!) {
            viewModel.newRecords -= record
            viewModel.newRecords += record.copy(sessionPosition = record.sessionPosition - 1)
        }
    }
    viewModel.positionBeingModified = null

    return viewModel.newRecords
}