package com.rib.progressiverecords.ui

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.Card
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session
import com.rib.progressiverecords.model.TimeLength
import com.rib.progressiverecords.ui.theme.StandardButton
import com.rib.progressiverecords.ui.theme.StandardOutlinedButton
import com.rib.progressiverecords.ui.theme.StandardTextField
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun SessionCreationScreen(
    viewModel: SessionViewModel,
    navController: NavController
) {
    val addingExercise = rememberSaveable { mutableStateOf(false) }

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
            viewModel = viewModel,
            navController = navController
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
                onSessionSaved = { saveSessionToDb(
                    viewModel = viewModel,
                    navController = navController,
                    records = records,
                    session = session
                ) },
                viewModel = viewModel
            )
        }

        if (addingExercise.value) {
            SelectExerciseDialog(
                onDismissRequest = { addingExercise.value = false },
                onExerciseSelected = { exerciseName ->
                    addingExercise.value = false

                    //val category = viewModel.getCategoryWithExerciseName(exerciseName)

                    val lastExercisePosition = if (records.isEmpty()) { 0 } else { records[records.lastIndex].sessionPosition }

                    records = records + createNewRecord(
                        sessionId = session.id,
                        previousSet = 0,
                        exerciseName = exerciseName,
                        sessionPosition = lastExercisePosition + 1,
                        category = ""
                    )
                    viewModel.newRecords = records
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
    viewModel: SessionViewModel,
    navController: NavController
) {
    if (exerciseSetsList.totalSets.isEmpty()) {
        Column (modifier = Modifier.fillMaxSize()) {
            SessionNameAndDate(
                session = session,
                onOpenDateDialog = { onOpenDateDialog() },
                onUpdateSessionName = { onUpdateSessionName(it) }
            )

            AddExerciseButton(selectExercise = { selectExercise() })

            Spacer (modifier = Modifier.weight(1f))

            CancelButton(viewModel = viewModel, navController = navController)
        }

    } else {
        LazyColumn {
            item {
                SessionNameAndDate(
                    session = session,
                    onOpenDateDialog = { onOpenDateDialog() },
                    onUpdateSessionName = { onUpdateSessionName(it) }
                )
            }

            items(exerciseSetsList.totalSets) {setList  ->
                if (setList.isNotEmpty()) {
                    ExerciseSets(
                        sets = setList,
                        sessionId = session.id,
                        onUpdateRecords = { onUpdateRecords(it) }
                    )
                }
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

@Composable
private fun SessionNameAndDate (
    session: Session,
    onOpenDateDialog: () -> Unit,
    onUpdateSessionName: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(session.sessionName) }

    Row (
        modifier = Modifier
            .padding(16.dp),
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
    selectExercise: () -> Unit,
    exerciseName: String
) {
    Column {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { selectExercise() }
        ) {
            Text(
                text = exerciseName,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                Icons.Filled.MoreVert,
                tint = MaterialTheme.colors.onBackground,
                contentDescription = stringResource(R.string.change_exercise_icon_description)
            )
        }

        Row (
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
                ) {
            Text (
                modifier = Modifier
                    .weight(0.5f)
                    .padding(4.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.set_label),
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text (
                text = stringResource(R.string.weight_label),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )

            Text(
                text = stringResource(R.string.repetitions_label),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
private fun ExerciseSets(
    sets: List<Record>,
    sessionId: UUID,
    onUpdateRecords: (Record) -> Unit
) {
    val exerciseName = sets[0].exerciseName
    Column (
        modifier = Modifier
            .padding(8.dp)
            ) {
        ExerciseHeader(
            selectExercise = { /*TODO*/ },
            exerciseName = exerciseName
        )

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
            .padding(8.dp)
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
            isEnabled = !recordIsSaved
        )

        StandardTextField(
            entryValue = repetitions,
            onValueChange = { repetitions = it },
            modifier = Modifier
                .weight(1f),
            isNumeric = true,
            isEnabled = !recordIsSaved
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
            colors = CheckboxDefaults.colors(checkedColor = Color.Green),
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
    StandardOutlinedButton (
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
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
private fun AddExerciseButton(
    selectExercise: () -> Unit
) {
    StandardOutlinedButton(
        onClick = { selectExercise() },
        text = stringResource(R.string.add_exercise_button),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

@Composable
private fun CancelButton (
    viewModel: SessionViewModel,
    navController: NavController
) {
    StandardOutlinedButton(
        onClick = { cancelAndDelete(viewModel, navController) },
        text = stringResource(R.string.cancel_session_button),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        textColor = Color.Red
    )
}

@Composable
private fun SelectExerciseDialog (
    onDismissRequest: () -> Unit,
    onExerciseSelected: (String) -> Unit
) {
    Dialog (
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismissRequest() }
    ) {
        Surface (modifier = Modifier.fillMaxSize()) {
            ExerciseScreen(
                viewModel = ExerciseViewModel(),
                isBeingSelected = true,
                onExerciseSelected = { onExerciseSelected(it) }
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
            modifier = Modifier.background(color = MaterialTheme.colors.primary)
                ) {
            if (viewModel.newRecords.isNotEmpty()) {
                Column (modifier = Modifier.padding(16.dp)) {
                    Text (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = stringResource(R.string.save_session_top_message),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = stringResource(R.string.save_session_bottom_message),
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    Row (
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton (
                            onClick = { onDismissRequest() },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel_button),
                                color = MaterialTheme.colors.secondary
                            )
                        }

                        TextButton (
                            onClick = { onSessionSaved() },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.confirm_button),
                                color = MaterialTheme.colors.secondary
                            )
                        }
                    }
                }
            } else {
                Column {
                    Text (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = stringResource(R.string.save_session_error_message),
                        style = MaterialTheme.typography.body2,
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
    val reps = if (category != "duration") { 0 } else { null }
    val weight = if (category != "repsOnly" && category != "duration") { 0f } else { null }
    val duration = if (category == "duration") { TimeLength(0, 0, 0) } else { null }

    return Record(
        id = UUID.randomUUID(),
        sessionId = sessionId,
        exerciseName = exerciseName,
        sessionPosition = sessionPosition,
        setNumber = previousSet + 1,
        repetitions = reps,
        weight = weight,
        exerciseDuration = duration
    )
}

private fun deleteSetInPosition(
    viewModel: SessionViewModel,
    position: Int
) {
    viewModel.newRecords.forEach { record ->
        if (record.sessionPosition == position) {
            viewModel.newRecords -= record
        }
    }
}